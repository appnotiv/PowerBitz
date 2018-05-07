package com.powerbtc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.powerbtc.Constants.AppGlobal;
import com.powerbtc.Constants.WsConstant;
import com.powerbtc.MainActivity;
import com.powerbtc.R;
import com.powerbtc.activity.BarCodeScanActivity;
import com.powerbtc.model.CommonStatusResponce;
import com.powerbtc.webservice.RestClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class FragmentSend extends Fragment {
    private LinearLayout lvPaste, lvQrCode;
    private TextView tvBalance, tvTotalAmount, tvSubmit,tvFees, edFees;
    private EditText edAddress, edAmount, edTrans;
    private double mainFee = 0, total = 0;
    public FragmentSend() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.mainTitle.setText("Send PBZ");
        init(view);
        setClickEvent();
    }

    private void init(View view) {
        lvPaste = (LinearLayout) view.findViewById(R.id.linear_Send_TRC_Paste);
        lvQrCode = (LinearLayout) view.findViewById(R.id.linear_Send_TRC_QrCode);

        tvBalance = (TextView) view.findViewById(R.id.tv_Send_TRC_Balance);
        tvTotalAmount = (TextView) view.findViewById(R.id.tv_Send_TRC_TotalAmount);
        tvFees = (TextView) view.findViewById(R.id.tv_Send_Fees);
        tvSubmit = (TextView) view.findViewById(R.id.tv_Send_TRC_Submit);
        edFees = (TextView) view.findViewById(R.id.tv_Send_TRC_Fees);

        edAddress = (EditText) view.findViewById(R.id.ed_Send_TRC_Address);
        edAmount = (EditText) view.findViewById(R.id.ed_Send_TRC_Amount);
        edTrans = (EditText) view.findViewById(R.id.ed_Send_TRC_TransPassword);

        if (!AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_FEES).equalsIgnoreCase("")) {
            mainFee = Double.parseDouble(AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_FEES));
        }

        tvBalance.setText("Balance : " + AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_BALANCE) + " PBZ");
        tvFees.setText("Fees : " + AppGlobal.getStringPreference(getActivity(), WsConstant.SP_WALLET_GUC_FEES) + " %");
    }

    private void setClickEvent() {
        edAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (edAmount.getText().toString().trim().equalsIgnoreCase("") ||
                        edAmount.getText().toString().trim().equalsIgnoreCase("0") ||
                        edAmount.getText().toString().equalsIgnoreCase(".")) {
                    tvTotalAmount.setText("0");
                    tvFees.setText("0");
                } else {
                    double mainAmount = Double.parseDouble(edAmount.getText().toString());
                    double fees = (mainAmount * mainFee) / 100;

                    total = mainAmount + fees;

                    edFees.setText(String.format("%.8f", fees));
                    tvTotalAmount.setText(String.format("%.8f", total));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        lvPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edAddress.setText(AppGlobal.pasteData(getActivity()));
                edAddress.setSelection(edAddress.getText().length());
            }
        });

        lvQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarCodeScanActivity.class);
                startActivityForResult(i, 101);
            }
        });

        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edAddress.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getActivity(), "Please enter PBZ address", Toast.LENGTH_SHORT).show();
                } else if (edAmount.getText().toString().trim().equalsIgnoreCase("") ||
                        edAmount.getText().toString().trim().equalsIgnoreCase("0")) {
                    Toast.makeText(getActivity(), "Please enter valid amount", Toast.LENGTH_SHORT).show();
                } else if (edTrans.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(getActivity(), "Please enter Login password", Toast.LENGTH_SHORT).show();
                } else {
                    doTRC();
                }
            }
        });

    }

    public void doTRC() {
        if (AppGlobal.isNetwork(getActivity())) {

            AppGlobal.showProgressDialog(getActivity());
            Map<String, String> optioMap = new HashMap<>();
            optioMap.put("RegisterId", AppGlobal.getStringPreference(getActivity(), WsConstant.SP_LOGIN_REGID));
            optioMap.put("ToAddress", edAddress.getText().toString().trim());
            optioMap.put("OtherAmount", edAmount.getText().toString().trim());
            optioMap.put("LoginPassword", edTrans.getText().toString().trim());
            optioMap.put("ValidData", AppGlobal.createAPIString());

            new RestClient(getActivity()).getInstance().get().soSend(optioMap).enqueue(new Callback<CommonStatusResponce>() {
                @Override
                public void onResponse(Call<CommonStatusResponce> call, Response<CommonStatusResponce> response) {
                    AppGlobal.hideProgressDialog(getActivity());
                    if (response.body() != null) {
                        if (response.body().getSuccess() == 1) {
                            Toast.makeText(getActivity(), "" + response.body().getMsg(), Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getActivity(), "" + response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CommonStatusResponce> call, Throwable t) {
                    try {
                        AppGlobal.hideProgressDialog(getActivity());
                        Toast.makeText(getActivity(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == 101) {
            if (data != null) {
                Log.e("Ritzpay", "Scanned: " + data.getStringExtra("Barcode"));
                String contents = data.getStringExtra("Barcode");

                if (contents.contains("bitcoins:")) {
                    contents = contents.replace("bitcoins:", "");
                }
                if (contents.contains("?")) {
                    //   contents=contents.split("\\?")[0];
                    edAddress.setText(contents.split("\\?")[0]);
                } else {
                    edAddress.setText(contents);

                }
                edAddress.setSelection(edAddress.getText().length());

            }
        }
    }
}