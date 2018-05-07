package com.powerbtc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powerbtc.R;
import com.powerbtc.model.TransactionResponse;

import java.util.ArrayList;


@SuppressWarnings("ALL")
public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.MyViewHolder> {

    ArrayList<TransactionResponse.Info> listData;
    Context mContext;


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvFee, tvAmount, tvDes, tvDate;

        public MyViewHolder(View view) {
            super(view);

            tvType = (TextView) view.findViewById(R.id.tv_ItemLendingHistory_Type);
            tvFee = (TextView) view.findViewById(R.id.tv_ItemLendingHistory_Fee);
            tvAmount = (TextView) view.findViewById(R.id.tv_ItemLendingHistory_Amount);
            tvDes = (TextView) view.findViewById(R.id.tv_ItemLendingHistory_Description);
            tvDate = (TextView) view.findViewById(R.id.tv_ItemLendingHistory_Date);
        }
    }

    public TransactionHistoryAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public TransactionHistoryAdapter(Context mContext, ArrayList<TransactionResponse.Info> listData) {
        this.mContext = mContext;
        this.listData = listData;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_transaction_history, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.tvFee.setText("Tx Fee : " + listData.get(position).getTransactionFee());

        holder.tvAmount.setText("" + listData.get(position).getOtherAmount());

        if (listData.get(position).getTransactionType() != null) {
            holder.tvType.setText("" + listData.get(position).getTransactionType());
            if (listData.get(position).getTransactionType().equalsIgnoreCase("Send")) {
                holder.tvAmount.setTextColor(Color.parseColor("#fe3e3e"));
//                holder.tvFee.setVisibility(View.VISIBLE);
            } else {
                holder.tvAmount.setTextColor(Color.parseColor("#4d804d"));
//                holder.tvFee.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.tvType.setText("");
        }

        String des = "Sent To:" + listData.get(position).getToAddress() + "\n" + "Txid:" +
                listData.get(position).getTransactionHash();

        holder.tvDes.setText("" + des);
        holder.tvDate.setText("" + listData.get(position).getTransactionDate());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}