package cn.zhuguangsheng.android.nfcm1demo;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    TextView promt;
    EditText etReadPwd;

    String strReadPwd = "FFFFFFFFFFFF";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promt = (TextView) findViewById(R.id.promt);
        etReadPwd = (EditText) findViewById(R.id.etReadPwd);
        // 获取默认的NFC控制器
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            promt.setText("设备不支持NFC！");
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            promt.setText("请在系统设置中先启用NFC功能！");
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            //处理该intent
            processIntent(getIntent());
        }

        hideSoftKeyBoard();
    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private void hideSoftKeyBoard() {
        View view = getWindow().peekDecorView();
        if (view != null){
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        //读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                byte[] readPwdBytes = hexStringToBytes(strReadPwd);
                if (readPwdBytes == null) {
                    Toast.makeText(getApplicationContext(), "password format error 密码格式不对", Toast.LENGTH_SHORT).show();
                }
                auth = mfc.authenticateSectorWithKeyA(j,
                        readPwdBytes);
                //MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            promt.setText(metaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
