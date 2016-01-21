package teamunguided.postboard;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import android.app.ActionBar;

import hollowsoft.slidingdrawer.OnDrawerCloseListener;
import hollowsoft.slidingdrawer.OnDrawerOpenListener;
import hollowsoft.slidingdrawer.OnDrawerScrollListener;
import hollowsoft.slidingdrawer.SlidingDrawer;

public class ListViewActivity extends Activity implements OnClickListener {
    private Button btnAdd;
    private EditText et;
    private ListView lv;
    ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        btnAdd = (Button) findViewById(R.id.addTaskBtn);
        btnAdd.setOnClickListener(this);
        et = (EditText) findViewById(R.id.editText);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list);

        // set the lv variable to your list in the xml
        lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);
    }

    public void onClick(View v) {
        String input = et.getText().toString();
        if (input.length() > 0) {
            // add string to the adapter, not the listview
            adapter.add(input);
            // no need to call adapter.notifyDataSetChanged(); as it is done by the adapter.add() method
        }
    }

    public class DrawerActivity extends Activity implements OnDrawerOpenListener,
            OnDrawerCloseListener,
            OnDrawerScrollListener {

        private static final String TAG = "DrawerActivity";

        @Override
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);

            setContentView(R.layout.drawer_activity);

            final SlidingDrawer drawer = (SlidingDrawer) view.findViewById(R.id.drawer);

            drawer.setOnDrawerOpenListener(this);
            drawer.setOnDrawerCloseListener(this);
            drawer.setOnDrawerScrollListener(this);
        }

        @Override
        public void onDrawerOpened() {
            Log.i(TAG, "Drawer Opened");
        }

        @Override
        public void onDrawerClosed() {
            Log.i(TAG, "Drawer Closed");
        }

        @Override
        public void onScrollStarted() {
            Log.i(TAG, "Scroll Started");
        }

        @Override
        public void onScrollEnded() {
            Log.i(TAG, "Scroll Ended");
        }
    }
}
