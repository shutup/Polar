package com.shutup.polar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by shutup on 2017/4/29.
 */

public class PkgListAdapter extends RecyclerView.Adapter<PkgListAdapter.MyViewHolder> implements Constants {
    private List<PInfo> mPInfos;
    private Context mContext;
    private int current_status = ACTIVITY_NORMAL;

    public PkgListAdapter(List<PInfo> PInfos, Context context) {
        mPInfos = PInfos;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pkg_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        PInfo pInfo = mPInfos.get(position);
        try {
            Drawable icon = mContext.getPackageManager().getApplicationIcon(pInfo.getPkgName());
            if (icon == null) {
                icon = mContext.getResources().getDrawable(R.mipmap.ic_launcher);
            }
            holder.mPkgIcon.setBackgroundDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.mPkgName.setText(pInfo.getAppName());
        if (current_status == ACTIVITY_NORMAL) {
            if (pInfo.isEnable()) {
                holder.mPkgStatus.setVisibility(View.INVISIBLE);
            }else {
                holder.mPkgStatus.setVisibility(View.VISIBLE);
            }
            holder.mPkgEnable.setVisibility(View.INVISIBLE);
        } else if (current_status == ACTIVITY_EDIT) {
            holder.mPkgStatus.setVisibility(View.INVISIBLE);
            holder.mPkgEnable.setVisibility(View.VISIBLE);
            holder.mPkgEnable.setChecked(!pInfo.isEnable());
        }
    }

    @Override
    public int getItemCount() {
        return mPInfos.size();
    }

    public void setPInfos(List<PInfo> PInfos) {
        mPInfos = PInfos;
    }

    public void setCurrent_status(int current_status) {
        this.current_status = current_status;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.pkg_icon)
        ImageView mPkgIcon;
        @InjectView(R.id.pkg_enable)
        CheckBox mPkgEnable;
        @InjectView(R.id.pkg_status)
        ImageView mPkgStatus;
        @InjectView(R.id.pkg_name)
        TextView mPkgName;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
