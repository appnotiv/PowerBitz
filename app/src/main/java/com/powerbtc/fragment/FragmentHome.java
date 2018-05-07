package com.powerbtc.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.powerbtc.Constants.AppGlobal;
import com.powerbtc.Constants.WsConstant;
import com.powerbtc.MainActivity;
import com.powerbtc.R;
import com.powerbtc.adapter.TransactionHistoryAdapter;
import com.powerbtc.model.LoginResponse;
import com.powerbtc.model.TransactionResponse;
import com.powerbtc.webservice.RestClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressWarnings("All")
public class FragmentHome extends Fragment {
    TextView walletAmount, tvBTC, tvSend, tvReceive;
    RecyclerView rvHomeHistory;
    ImageView imRefresh, imQrCode;
    MyWalletReceiver receiver;
    SwipeRefreshLayout swipeLayout;

    public FragmentHome() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        MainActivity.mainTitle.setText("Home");

        IntentFilter filter = new IntentFilter(MyWalletReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyWalletReceiver();
        getActivity().registerReceiver(receiver, filter);

        init(v);
        setClickEvent();
        setData();

//        doGetUserHistory("");

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        doGetUserData();
    }

    private void setData() {
        walletAmount.setText(AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_BALANCE) + " PBZ");

        imQrCode.setImageBitmap(encodeAsBitmap(AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_ADDRESS)));

        doGetUserHistory("swipe");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
    }

    private void init(View v) {
        walletAmount = (TextView) v.findViewById(R.id.fragment_home_wallet_amount);
        tvBTC = (TextView) v.findViewById(R.id.fragment_home_wallet_BTC);

        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.SwipeLayout_Home);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);

                doGetUserHistory("swipe");
            }
        });

        imRefresh = (ImageView) v.findViewById(R.id.image_Home_Refresh);

        rvHomeHistory = (RecyclerView) v.findViewById(R.id.recyclerView_Home);
        rvHomeHistory.setLayoutManager(new LinearLayoutManager(getActivity()));

        imQrCode = (ImageView) v.findViewById(R.id.image_Home_QrCode);

        tvSend = (TextView) v.findViewById(R.id.tv_Home_Send);
        tvReceive = (TextView) v.findViewById(R.id.tv_Home_Receive);
    }

    private void setClickEvent() {
        imRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                doGetWalletData();
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).callFragment(new FragmentSend());
            }
        });

        tvReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).callFragment(new FragmentMyAddressALC());
            }
        });
    }

    public void doGetUserHistory(String type) {
        if (AppGlobal.isNetwork(getActivity())) {
            Map<String, String> optioMap = new HashMap<>();
            optioMap.put("RegisterId", AppGlobal.getStringPreference(getActivity(), WsConstant.SP_LOGIN_REGID));
            optioMap.put("ValidData", AppGlobal.createAPIString());

            if (type.equalsIgnoreCase("")) {
                AppGlobal.showProgressDialog(getActivity());
            }

            new RestClient(getActivity()).getInstance().get().getHistory(optioMap).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    AppGlobal.hideProgressDialog(getActivity());
                    if (swipeLayout.isRefreshing()) {
                        swipeLayout.setRefreshing(false);
                    }
                    if (response.body() != null) {
                        if (response.body().getSuccess() == 1) {
                            if (response.body().getInfo().size() > 0) {
                                TransactionHistoryAdapter adapter = new TransactionHistoryAdapter(getActivity(), response.body().getInfo());
                                rvHomeHistory.setAdapter(adapter);
                            } else {
                                Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    AppGlobal.hideProgressDialog(getActivity());
                    if (swipeLayout.isRefreshing()) {
                        swipeLayout.setRefreshing(false);
                    }
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Bitmap encodeAsBitmap(String str) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Bitmap bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
            ;
            try {
                BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 256, 256);
                int width = 256;
                int height = 256;
                bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (bitMatrix.get(x, y))
                            bmp.setPixel(x, y, Color.BLACK);
                        else
                            bmp.setPixel(x, y, Color.WHITE);
                    }
                }

            } catch (WriterException e) {
                //Log.e("QR ERROR", ""+e);
            }
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    public void doGetUserData() {
        if (AppGlobal.isNetwork(getActivity())) {

            Map<String, String> optioMap = new HashMap<>();
            optioMap.put("UserName", AppGlobal.getStringPreference(getActivity(), WsConstant.SP_LOGIN_EMAIL));
            optioMap.put("Password", AppGlobal.getStringPreference(getActivity(), WsConstant.SP_LOGIN_PASSSWORD));
            optioMap.put("ValidData", AppGlobal.createAPIString());

            new RestClient(getActivity()).getInstance().get().UserLoginNew(optioMap).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.body() != null) {
                        if (response.body().getSuccess() == 1) {

                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getRegisterId(), WsConstant.SP_LOGIN_REGID);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getName(), WsConstant.SP_LOGIN_NAME);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getUsername(), WsConstant.SP_LOGIN_USERNAME);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getEmailId(), WsConstant.SP_LOGIN_EMAIL);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getMobileNo(), WsConstant.SP_LOGIN_MOBILE);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getProfile(), WsConstant.SP_LOGIN_PROFILE_PIC);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getLoginStatus(), WsConstant.SP_LOGIN_STATUS);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getEmailVerificationStatus(), WsConstant.SP_LOGIN_EMAIL_VERIFY);

                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getResidentAddress(), WsConstant.SP_ADDRESS_RES);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getCity(), WsConstant.SP_CITY);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getState(), WsConstant.SP_STATE);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getPincode(), WsConstant.SP_PICODE);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getCountryName(), WsConstant.SP_COUNTRY);
                            AppGlobal.setStringPreference(getActivity(), response.body().getInfo().getCountryCode(), WsConstant.SP_COUNTRY_CODE);

                            MainActivity.setUserData();

                        } else {
                            AppGlobal.logoutApplication(getActivity());
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    try {
                        Toast.makeText(getActivity(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    public class MyWebRequestReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.PBZ.Activity.intent.action.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                setData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class MyWalletReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "cears.chiba.Activity.intent.action.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                setData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}