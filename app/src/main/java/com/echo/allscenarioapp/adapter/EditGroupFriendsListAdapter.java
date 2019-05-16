package com.echo.allscenarioapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.allscenarioapp.R;
import com.echo.allscenarioapp.activity.EditGroupActivity;
import com.echo.allscenarioapp.activity.OneToOneChatActivity;
import com.echo.allscenarioapp.model.FriendsListModel;
import com.echo.allscenarioapp.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by EchoIT on 11/29/2018.
 */

public class EditGroupFriendsListAdapter extends RecyclerView.Adapter<EditGroupFriendsListAdapter.MyViewHolder> {

    Context context;
    private LayoutInflater inflater = null;
    public ArrayList<FriendsListModel> objList = null;
    private View.OnClickListener clickListener=null;
    View.OnLongClickListener onLongClickListener = null;
    public ArrayList<String> chkIDs = new ArrayList<String>();;
    boolean isCheckCall = false;
    public int count = 0;
    public boolean isCheckChange = false;
    public EditGroupFriendsListAdapter(Context context, View.OnClickListener clickListener,View.OnLongClickListener onLongClickListener) {
        this.context = context;
        objList = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickListener =clickListener;
        this.onLongClickListener = onLongClickListener;
        chkIDs = new ArrayList<String>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_member_list, viewGroup, false);
        Utils.applyFontFace(context, itemView);
        return new MyViewHolder(itemView);
    }

    public void addData(ArrayList<FriendsListModel> mobjList) {
        objList = new ArrayList<>();
        if (mobjList != null && mobjList.size() > 0)
            objList.addAll(mobjList);

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.row.setTag(position);
        holder.row.setOnClickListener(clickListener);
        holder.row.setOnLongClickListener(onLongClickListener);

        holder.imgProfilePic.setActualImageResource(R.drawable.avatar);
        if (objList.get(position).image != null && !objList.get(position).image.isEmpty()) {
            holder.imgProfilePic.setImageURI(Uri.parse(objList.get(position).image));
        }

        holder.txtUserName.setText(objList.get(position).fullname);
        holder.txtEmail.setText(objList.get(position).email);

        holder.txtAdmin.setVisibility(objList.get(position).is_admin.equalsIgnoreCase("1")?View.VISIBLE:View.GONE);


    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objList != null ? objList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.row)
        RelativeLayout row;
        @BindView(R.id.imgProfilePic)
        SimpleDraweeView imgProfilePic;
        @BindView(R.id.txtUserName)
        TextView txtUserName;
        @BindView(R.id.txtEmail)
        TextView txtEmail;
        @BindView(R.id.txtAdmin)
        TextView txtAdmin;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
