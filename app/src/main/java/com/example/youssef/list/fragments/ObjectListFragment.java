package com.example.youssef.list.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youssef.list.adapters.ItemsAdapter;
import com.example.youssef.list.models.Item;
import com.example.youssef.list.models.Item_Table;
import com.example.youssef.list.presenters.ListPresenter;
import com.example.youssef.list.R;
import com.example.youssef.list.models.User;
import com.example.youssef.list.presenters.PresenterCache;
import com.example.youssef.list.presenters.PresenterFactory;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

/**
 * Created by Youssef on 7/30/2016.
 */

public class ObjectListFragment extends Fragment implements  View.OnClickListener {

    @Inject Retrofit retrofit;
    private PresenterCache presenterCache =
            PresenterCache.getInstance();
    private PresenterFactory<ListPresenter> presenterFactory =
            new PresenterFactory<ListPresenter>() {
                @NonNull
                @Override
                public ListPresenter createPresenter() {
                    return new ListPresenter();
                }
            };
    private boolean isDestroyedBySystem;
    private ListPresenter presenter;

    RestTemplate restTemplate = new RestTemplate();
    Runnable mServerRunnable;
    ListPresenter listPresenter;
    Thread mServerThread;
    Boolean isRequest = false;
    AlertDialog dba;
    public Dialog dbRetry;
    public String mToken;
    public User mCurUser;
    Context mContext;
    Runnable uiRunnable;
    @BindView(R.id.objects_list_layout) public SwipeRefreshLayout mObjectsListLayout;
    @BindView(R.id.objects_list) ListView mObjectsList;
    @BindView(R.id.items_added) TextView mAddedItemsText;
    private ItemsAdapter mObjectsAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        InjectorClass
        setHasOptionsMenu(true);
        setRetainInstance(true);
        presenter = presenterCache.getPresenter(ListPresenter.TAG,
                presenterFactory);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_list,container,false);
    }

    private void showDialogText(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle(getString(R.string.add_title));
        db.setMessage(getString(R.string.new_item));
        final EditText objectT = new EditText(mContext);
        objectT.setImeOptions(EditorInfo.IME_ACTION_DONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        objectT.setLayoutParams(lp);
        db.setView(objectT);
        db.setPositiveButton(getString(R.string.action_add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = objectT.getText().toString();
//                mObjectsAdapter.addItem(item);
                presenter.addItemRetrofit(item);
            }
        });
        db.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dba = db.create();
        objectT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dba.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        objectT.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dba.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh:
                presenter.fetchListRetrofit();
                return true;
            case R.id.action_add:
                if(mCurUser.getCompany()!=null)
                    showDialogText();
                else
                    Toast.makeText(getActivity(),getString(R.string.error_no_group_add),Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_menu:
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                if(dl.isDrawerOpen(Gravity.LEFT))
                    dl.closeDrawer(Gravity.LEFT);
                else
                    dl.openDrawer(Gravity.LEFT);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onNext(final ArrayList<String> array, final ArrayList<Boolean> checked){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mObjectsAdapter.clear();
                String text = "";
                for (int i = 0; i < array.size(); i++){
                    mObjectsAdapter.addItem(array.get(i), checked.get(i));

                }
                List<Item> items = new Select().from(Item.class).orderBy(Item_Table.occurences, false).queryList();
                for(int i=0; i<items.size(); i++){
                    text += items.get(i).getItemName();
                    text += items.get(i).occurences + " ";
                }
                mObjectsAdapter.notifyDataSetChanged();
                mAddedItemsText.setText(text);
            }
        });

    }
    private void setupToolbar() {

        ImageView addItem = (ImageView) getActivity().findViewById(R.id.action_add);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurUser.getCompany()!=null)
                    showDialogText();
                else
                    Toast.makeText(getActivity(),getString(R.string.error_no_group_add),Toast.LENGTH_LONG).show();
            }
        });
        ImageView refresh = (ImageView) getActivity().findViewById(R.id.action_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.fetchListRetrofit();
            }
        });
        ImageView menu = (ImageView) getActivity().findViewById(R.id.action_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                if(dl.isDrawerOpen(Gravity.LEFT))
                    dl.closeDrawer(Gravity.LEFT);
                else
                    dl.openDrawer(Gravity.LEFT);            }
        });
    }


    public void showError(final String error){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(error.equals(getString(R.string.error_not_connected)))
                {
                    mObjectsAdapter.clear();
                }
                if(dba!=null && dba.isShowing())
                    dba.dismiss();
                final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
                db.setTitle(getString(R.string.error_title_generic));
                db.setMessage(error);
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.retry_dialog_layout, null);
                db.setView(dialogView);
                dbRetry  = db.create();
                dbRetry.show();
                Button retryButton = (Button)dbRetry.findViewById(R.id.retry);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences sharedPref = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username","");
                        String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                        Message msg = new Message();
                        msg.getData().putString("caller","fetchList");
                        presenter.loginRetrofit(username, password,msg);
                    }
                });
                TextView title = (TextView)dbRetry.findViewById(android.R.id.title);
                if(title!=null)
                    title.setGravity(Gravity.CENTER);
                TextView msg = (TextView)dbRetry.findViewById(android.R.id.message);
                if(msg!=null)
                    msg.setGravity(Gravity.CENTER);
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        presenter.takeView(null);
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        if(presenter == null)
            presenter = presenterCache.getPresenter(ListPresenter.TAG,presenterFactory);
        presenter.takeView(this);
//        if(mCurUser.getCompany() != null)
//            fetchList();
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if(dba!=null && dba.isShowing())
            dba.dismiss();
        if(dbRetry!=null && dbRetry.isShowing())
            dba.dismiss();
        getActivity().finish();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mContext = view.getContext();
        ButterKnife.bind(this, view);
        setupToolbar();
//        LinearLayout ll = (LinearLayout) view;
//        mObjectsList = (ListView)ll.findViewById(R.id.objects_list);
        mObjectsAdapter = new ItemsAdapter(mContext, presenter);
        mObjectsList.setAdapter(mObjectsAdapter);
        mCurUser = (User) this.getArguments().getSerializable("user");
        mToken = this.getArguments().getString("token");
        mObjectsListLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.fetchListRetrofit();
            }
        });
//        presenter = new ListPresenter();
        presenter.takeView(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.action_refresh:
                presenter.fetchListRetrofit();
                break;
            case R.id.action_add:
                if(mCurUser.getCompany()!=null)
                    showDialogText();
                else
                    Toast.makeText(getActivity(),getString(R.string.error_no_group_add),Toast.LENGTH_LONG).show();
                break;
            case R.id.action_menu:
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                if(dl.isDrawerOpen(Gravity.LEFT))
                    dl.closeDrawer(Gravity.LEFT);
                else
                    dl.openDrawer(Gravity.LEFT);
                break;
            default:
                break;
        }
    }
}
