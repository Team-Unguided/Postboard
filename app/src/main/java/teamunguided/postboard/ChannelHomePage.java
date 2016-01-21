package teamunguided.postboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;


import com.magnet.mmx.client.api.ListResult;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;

import java.nio.channels.Channel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import teamunguided.postboard.adapters.BaseInflaterAdapter;
import teamunguided.postboard.adapters.CardItemData;
import teamunguided.postboard.adapters.inflaters.CardInflater;

public class ChannelHomePage extends Activity {
    private static final String TAG = ChannelHomePage.class.getSimpleName();
    private static final String KEY_MESSAGE_TEXT = "content";
    static final int REQUEST_LOGIN = 1;
    public static final String EXTRA_CHANNEL_NAME = "channelName";

    ListView list = (ListView) findViewById(R.id.list_view);
    private MyProfile mProfile;
    private List<MMXChannel> mChannels;
    private MMXChannel mChannel;
    private MMX.EventListener mListener = new MMX.EventListener() {
        public boolean onMessageReceived(com.magnet.mmx.client.api.MMXMessage mmxMessage) {
            MMXChannel channel = mmxMessage.getChannel();
            if (channel != null && channel.getName().equals(mChannel.getName())) {
                updateChannelItems();
            }
            return true;
        }
    };

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //TODO:change the click action to bring you to an edit screen
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                String channelName = (String) view.getTag();
                if(channelName != null){
                    Intent intent = new Intent(ChannelHomePage.this, ListViewActivity.class);
                    intent.putExtra(EXTRA_CHANNEL_NAME, channelName);
                    startActivity(intent);
                }
            }
        });
        updateChannelItems();
    }

    protected void onResume() {
        super.onResume();
        if (MMX.getCurrentUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_LOGIN);
        } else {
            //populate or update the view
            updateChannelItems();
        }
    }

    public void doBack(View view) {
        onBackPressed();
    }

    private void updateChannelItems() {
        synchronized (this) {
            if (mChannels != null) {
                ListView list = (ListView) findViewById(R.id.list_view);

                list.addHeaderView(new View(this));
                list.addFooterView(new View(this));

                BaseInflaterAdapter<CardItemData> adapter = new BaseInflaterAdapter<CardItemData>(new CardInflater());
                for (int i = 0; i < mChannels.size(); i++) {
                    CardItemData data = new CardItemData((mChannels.get(i).getName()));
                    adapter.addItem(data, false);
                }

                list.setAdapter(adapter);
            }
        }
    }

    public void doShowMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_channel_item_list, popup.getMenu());

        //decide which items to hide
        Menu menu = popup.getMenu();

        if (!mChannel.getOwnerUsername()
                .equalsIgnoreCase(MMX.getCurrentUser().getUsername())) {
            menu.removeItem(R.id.action_delete);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_unsubscribe:
                        doUnsubscribe();
                        break;
                    case R.id.action_delete:
                        doDelete();
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    public void doSubscribe() {
        mChannel.subscribe(new MMXChannel.OnFinishedListener<String>() {
            public void onSuccess(String s) {
                Toast.makeText(ChannelHomePage.this, "Subscribed successfully", Toast.LENGTH_LONG).show();
            }

            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Toast.makeText(ChannelHomePage.this, "Unable to subscribe: " +
                        throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void doUnsubscribe() {
        mChannel.unsubscribe(new MMXChannel.OnFinishedListener<Boolean>() {
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(ChannelHomePage.this, "Unsubscribed successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ChannelHomePage.this, "Could not unsubscribe.", Toast.LENGTH_LONG).show();
                }
            }

            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Toast.makeText(ChannelHomePage.this, "Exception caught: " + throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void doDelete() {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mChannel.delete(new MMXChannel.OnFinishedListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                ChannelHomePage.this.finish();
                            }

                            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                                Toast.makeText(ChannelHomePage.this,
                                        getString(R.string.error_unable_to_delete_channel) +
                                                failureCode + ", " + throwable.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
                dialog.dismiss();
            }
        };
    }

    public void doLogout(View view) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        MMX.logout(new MMX.OnFinishedListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                ChannelHomePage.this.finish();
                            }

                            public void onFailure(MMX.FailureCode failureCode, Throwable throwable) {
                                Toast.makeText(ChannelHomePage.this, "Logout failed: " + failureCode +
                                        ", " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                .setTitle(R.string.dlg_signout_title)
                .setMessage(R.string.dlg_signout_message)
                .setPositiveButton(R.string.dlg_signout_ok, clickListener)
                .setNegativeButton(R.string.dlg_signout_cancel, clickListener);
        builder.create().show();
    }


}
