package cn.okayj.axui.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jack on 15/11/27.
 * DataNode的平坦索引,即目录.
 * 可以把节点看成书的章节和文章(叶子节点),平坦索引相当于目录,可以按顺序访问章节和文章.
 * 可以从该索引获取可见索引.
 * 索引都是动态的,跟随树型结构改变.
 */
public class NodeFlatIndex {
    private boolean mInvalidated = false;
    private boolean mIgnoreRoot = false;//是否忽略根节点的存在

    private DataNode mRootNode;

    private List<DataNode> mList = new LinkedList<>();
    private List<DataNode> mVisibleList;
    private VisibleFlatIndex mVisibleFlatIndex = new VisibleFlatIndex();

    NodeFlatIndex(DataNode rootNode){
        mRootNode = rootNode;
        addFlatNodes(null, null, rootNode);
    }

    /**
     * 销毁索引.
     * 如果不用索引了,但仍然需要使用对应但树结构,
     * 则最好销毁索引增加性能
     */
    public void invalidate(){
        mRootNode.invalidateFlatIndex();
        mRootNode = null;
        mList.clear();
        if(mVisibleList != null){
            mVisibleList.clear();
            mVisibleList = null;
        }
        mInvalidated = true;
    }

    public DataNode get(int position){
        if(mIgnoreRoot){
            int pos = position + 1;
            return mList.get(pos);
        }else {
            return mList.get(position);
        }
    }

    public int indexOf(DataNode dataNode){
        int index =  mList.indexOf(dataNode);
        if(mIgnoreRoot){
            return index - 1;
        }
        else {
            return index;
        }
    }

    public int size(){
        if(mIgnoreRoot){
            assert mList.size() > 0;
            return mList.size() - 1;
        }else {
            return mList.size();
        }
    }

    /**
     * 获取可见索引
     * @return
     */
    public VisibleFlatIndex getVisibleIndex(){
        if(mInvalidated){
            throw new RuntimeException("NodeFlatIndex is invalidated !!!");
        }

        if(mVisibleList == null){
            mVisibleList = new LinkedList<>();
            if(mRootNode.isVisible()) {
                addFlattedNodesToVisibleList(0, mRootNode, true);
            }
        }

        return mVisibleFlatIndex;
    }

    public void ignoreRoot(boolean ignoreRoot){
        mIgnoreRoot = ignoreRoot;
    }

    public boolean isIgnoreRoot(){
        return mIgnoreRoot;
    }


    /**
     * 将节点展平并添加到索引和可见索引(如果可见索引已创建到话)
     * @param preCousinNode
     * @param preVisibleCousinNode
     * @param addedNode
     */
    void addFlatNodes(DataNode preCousinNode,DataNode preVisibleCousinNode,DataNode addedNode){
        int basePosition = getBasePosition(preCousinNode, addedNode);
        int baseVisibleListPosition = 0;
        boolean addToVisibleList = false;
        baseVisibleListPosition = getVisibleBasePosition(preVisibleCousinNode, addedNode);
        if(mVisibleList != null && addedNode.isVisible() && baseVisibleListPosition >= 0){
            addToVisibleList = true;
        }

        addFlattedNodes(basePosition,baseVisibleListPosition,addedNode,addToVisibleList);
    }

    /**
     * 将节点树从索引中删除,如果有可见索引,将可见的节点从可见索引中删除
     * @param dataNode
     */
    void removeFlatNodes(DataNode dataNode){
        int position = mList.indexOf(dataNode);
        assert position >= 0;
        if(position >= 0){
            for (int i = 0; i < dataNode.getFlatSize();++i){
                try {
                    mList.remove(position);
                }catch (IndexOutOfBoundsException throwable){
                    throw new RuntimeException("索引状态不正确,节点树无法完全从索引删除,bug??",throwable);
                }
            }
        }

        if(mVisibleList != null && dataNode.isVisible()){
            int visiblePosition = mVisibleList.indexOf(dataNode);
            assert visiblePosition >= 0;
            if(visiblePosition > 0){
                for (int i = 0; i < dataNode.getVisibleFlatSize();++i){
                    mVisibleList.remove(visiblePosition);
                }
            }
        }
    }

    /**
     * 当某一节点的可见状态改变时被调用(被DataNode从外部调用),
     * 在此方法中更新可见索引
     * @param preVisibleCousinDataNode
     * @param dataNode
     * @param currentVisibility
     */
    void onNodeVisibilityChange(DataNode preVisibleCousinDataNode,DataNode dataNode,boolean currentVisibility){
        if(mVisibleList == null){
            return;
        }

        if(currentVisibility == true){
            int basePosition = getVisibleBasePosition(preVisibleCousinDataNode, dataNode);
            addFlattedNodesToVisibleList(basePosition, dataNode, true);
        }else {
            removeFlattedNodesFromVisibleList(dataNode, true);
        }
    }

    /**
     * 当某一节点的折叠状态被改变时被调用(外部调用),
     * 在此方法中更新可见索引
     * @param dataNode
     * @param currentFolded
     */
    void onNodeFoldStateChange(DataNode dataNode,boolean currentFolded){
        if(mVisibleList == null){
            return;
        }

        /*
        如果该节点本身不可见,则不会引起可见节点数量的改变
         */
        if(!dataNode.isVisible()){
            return;
        }

        if(currentFolded){
            removeFlattedNodesFromVisibleList(dataNode, false);
        }else {
            int basePosition = mVisibleList.indexOf(dataNode);
            assert basePosition >= 0;
            addFlattedNodesToVisibleList(++basePosition, dataNode, false);
        }
    }

    /**
     * 将变得不可见的节点树从可见索引中删除
     * @param dataNode 变为不可见的节点树的根
     * @param includeRootNode 是否连同该根节点一期删除
     */
    private void removeFlattedNodesFromVisibleList(DataNode dataNode,boolean includeRootNode){
        int basePosition = mVisibleList.indexOf(dataNode);
        assert basePosition >= 0;
        int removedSize = dataNode.getDescendantVisibleSize();
        if(includeRootNode){
            removedSize++;
        }else {
            basePosition++;
        }
        for (int i = 0; i < removedSize;++i){
            mVisibleList.remove(basePosition);
        }
    }

    /**
     * 在添加节点到可见列表前,可用此方法得到
     * 该节点在可见列表中应该在的位置,如果不应该在可见列表中,返回-1
     * @param preVisibleCousinNode
     * @param dataNode
     * @return
     */
    private int getVisibleBasePosition(DataNode preVisibleCousinNode,DataNode dataNode){
        DataNode parentNode = dataNode.getParentNode();
        if(parentNode == null){//根节点
            assert preVisibleCousinNode == null;
            return 0;
        }

        int parentVisiblePosition = mVisibleList.indexOf(parentNode);
        if(parentVisiblePosition < 0){
            return -1;
        }

        assert mVisibleList.indexOf(preVisibleCousinNode) >= 0;

        return mVisibleList.indexOf(preVisibleCousinNode) + preVisibleCousinNode.getVisibleFlatSize();

    }

    /**
     * 当节点树要添加到索引前,用此方法找到节点树在索引的基本位置(该节点树最前的位置)
     * @param preCousinNode 该节点树的同辈节点
     * @param dataNode 将要添加的节点树
     * @return
     */
    private int getBasePosition(DataNode preCousinNode,DataNode dataNode){
        DataNode parentNode = dataNode.getParentNode();
        if(preCousinNode == null){

            if(parentNode == null){
                /*
                *如果父亲节点为空,说明该节点本身是根节点,应该从列表最初开始添加
                */

                assert mList.size() == 0;
                /*if(mList.size() != 0){
                    throw new RuntimeException("添加根节点的索引大小应该为0");
                }*/
                return 0;
            }else {
                /*
                *添加的基础位置应该在其父亲节点之后
                 */


                int parentPosition = mList.indexOf(dataNode.getParentNode());
                assert parentPosition >= 0;
                return parentPosition + 1;
            }
        }else {
            /*
            *添加的基础位置应该在前一个兄妹节点展开之后最后一个节点之后
             */

            return mList.indexOf(preCousinNode) + preCousinNode.getFlatSize() /* - 1 + 1 */;
        }
    }

    /**
     * 将树展平并提取可见节点并添加到可见索引
     * @param basePosition 添加的节点树展开为可见列表后在可见列表中的最前位置
     * @param rootNode
     * @param includeRootNode
     */
    private void addFlattedNodesToVisibleList(int basePosition, DataNode rootNode,boolean includeRootNode){
        if(includeRootNode){
            mVisibleList.add(basePosition,rootNode);
            basePosition++;
        }

        /*
         *如果本节点未被添加到可见节点列表,或者本节点是折叠状态(子节点不可见),则子节点将不可见,不能添加到可见节点列表
         */
        boolean childHasChanceToAddToVisibleList;
        if(!rootNode.isFold()){
            childHasChanceToAddToVisibleList = true;
        }else {
            childHasChanceToAddToVisibleList = false;
        }

        for (int i = 0 ; i< rootNode.getHeaderNodeSize();++i){
            DataNode node = rootNode.getHeaderNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            if(childAddToVisibleList && node.isVisible()){
                addFlattedNodesToVisibleList(basePosition, node,true);
                basePosition += node.getVisibleFlatSize();
            }
        }

        for (int i = 0 ; i< rootNode.getChildNodeSize();++i){
            DataNode node = rootNode.getChildNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            if(childAddToVisibleList && node.isVisible()){
                addFlattedNodesToVisibleList(basePosition, node,true);
                basePosition += node.getVisibleFlatSize();
            }
        }

        for (int i = 0 ; i< rootNode.getFooterNodeSize();++i){
            DataNode node = rootNode.getFooterNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            if(childAddToVisibleList && node.isVisible()){
                addFlattedNodesToVisibleList(basePosition, node,true);
                basePosition += node.getVisibleFlatSize();
            }
        }
    }

    /**
     * 添加节点树到索引,如果有可见索引,则同时将可见的节点添加到可见索引
     * @param basePosition 添加的最前的位置,后代节点将添加到该位置之后
     * @param baseVisiblePosition 添加的可见索引的最前位置,后代节点将添加到该位置之后
     * @param rootNode 将要添加到节点树
     * @param addToVisibleList 是否要添加到可见索引(方法外调用时需要判断可见列表是否最在)
     */
    private void addFlattedNodes(int basePosition,int baseVisiblePosition,DataNode rootNode,boolean addToVisibleList){
        mList.add(basePosition,rootNode);
        basePosition++;
        if(addToVisibleList){
            mVisibleList.add(baseVisiblePosition,rootNode);
            baseVisiblePosition ++;
        }

        /*
         *如果本节点未被添加到可见节点列表,或者本节点是折叠状态(子节点不可见),则子节点将不可见,不能添加到可见节点列表
         */
        boolean childHasChanceToAddToVisibleList;
        if(addToVisibleList /*&& rootNode.isVisible()*/ && !rootNode.isFold()){
            childHasChanceToAddToVisibleList = true;
        }else {
            childHasChanceToAddToVisibleList = false;
        }

        for (int i = 0 ; i< rootNode.getHeaderNodeSize();++i){
            DataNode node = rootNode.getHeaderNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            addFlattedNodes(basePosition,baseVisiblePosition,node,childAddToVisibleList);
            basePosition += node.getFlatSize();
            if(childAddToVisibleList){
                baseVisiblePosition += node.getVisibleFlatSize();
            }
        }
        for (int i = 0 ; i< rootNode.getChildNodeSize();++i){
            DataNode node = rootNode.getChildNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            addFlattedNodes(basePosition,baseVisiblePosition,node,childAddToVisibleList);
            basePosition += node.getFlatSize();
            if(childAddToVisibleList){
                baseVisiblePosition += node.getVisibleFlatSize();
            }
        }
        for (int i = 0 ; i< rootNode.getFooterNodeSize();++i){
            DataNode node = rootNode.getFooterNode(i);
            boolean childAddToVisibleList = childHasChanceToAddToVisibleList && node.isVisible();
            addFlattedNodes(basePosition,baseVisiblePosition,node,childAddToVisibleList);
            basePosition += node.getFlatSize();
            if(childAddToVisibleList){
                baseVisiblePosition += node.getVisibleFlatSize();
            }
        }
    }

    public class VisibleFlatIndex {

        private VisibleFlatIndex() {

        }

        public DataNode get(int position){
            if(mIgnoreRoot && mVisibleList.get(0) == mRootNode){
                return mVisibleList.get(position + 1);
            }

            return mVisibleList.get(position);
        }

        public int indexOf(DataNode dataNode){
            int index = mVisibleList.indexOf(dataNode);

            if(mIgnoreRoot && mVisibleList.get(0) == mRootNode){
                return index - 1;
            }else {
                return index;
            }
        }

        public int size(){

            if(mIgnoreRoot && mVisibleList.get(0) == mRootNode){
                return mVisibleList.size() - 1;
            }else {
                return mVisibleList.size();
            }
        }

        public void invalidate(){
            mVisibleList.clear();
            mVisibleList = null;
        }
    }

}
