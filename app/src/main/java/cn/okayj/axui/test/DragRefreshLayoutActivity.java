package cn.okayj.axui.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by jack on 2017/2/28.
 */

public class DragRefreshLayoutActivity extends Activity {
    ListView listView;

    String[] strings = {
            "aaaaaaa",
            "bbbbbbb",
            "cccccc",
            "dddddddd",
            "eeeeeeee",
            "fffffffff",
            "ggggggggggg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.drag_refresh_layout_test);
            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(new ArrayAdapter<String>(this,R.layout.item,R.id.textView,strings));
    }
}
