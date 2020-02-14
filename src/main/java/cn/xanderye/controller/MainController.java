package cn.xanderye.controller;

import cn.xanderye.util.HttpUtil;
import cn.xanderye.util.IdCardUtil;
import cn.xanderye.util.MD5Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author XanderYe
 * @date 2020/2/6
 */
public class MainController implements Initializable {

    private final String codeUrlV1 = "/code/get_code";

    private final String requestUrlV1 = "/order/c4ca4238a0b923820dcc509a6f75849b";

    private final String codeUrlV2 = "/code/g5f98fdea3cf5f8fef59c2923b8ee6aeb";

    private final String requestUrlV2 = "/order/s5e3712a429b239a6f9c5d8ef0aa609cf";

    private final String BASE_URL = "https://kouzhaoserver.cxshzl.com/server/index.php/api/";

    private final String[] TOWNS = new String[]{"浒山街道", "古塘街道", "白沙路街道", "宗汉街道", "坎墩街道", "龙山镇", "掌起镇", "观海卫镇", "附海镇", "桥头镇", "匡堰镇", "逍林镇", "新浦镇", "胜山镇", "横河镇", "崇寿镇", "长河镇", "周巷镇", "庵东镇", "杭州湾新区"};

    private final Pattern phonePattern = Pattern.compile("^1([38]\\d|4[5-9]|5[0-35-9]|6[56]|7[0-8]|9[189])\\d{8}$");

    private final static Map<String, Object> HEADERS = new HashMap<>();

    static {
        HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36 QBCore/4.0.1295.400 QQBrowser/9.0.2524.400 Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2875.116 Safari/537.36 NetType/WIFI MicroMessenger/7.0.5 WindowsWechat");
        HEADERS.put("Referer", "https://kouzhao.cxshzl.com/wechat/");
        HEADERS.put("Origin", "https://kouzhao.cxshzl.com");
    }

    @FXML
    private TextField nameText;
    @FXML
    private TextField idText;
    @FXML
    private TextField phoneText;
    @FXML
    private TextField codeText;
    @FXML
    private ComboBox townBox;
    @FXML
    private TextField addressText;
    @FXML
    private TextArea logArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> options = FXCollections.observableArrayList(TOWNS);
        townBox.setItems(options);

        File file = new File(System.getProperty("user.dir") + File.separator + "config.properties");
        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(inputStream);
                nameText.setText(formatString(properties.getProperty("name")));
                idText.setText(formatString(properties.getProperty("idNo")));
                phoneText.setText(formatString(properties.getProperty("phone")));
                townBox.setValue(formatString(properties.getProperty("town")));
                addressText.setText(formatString(properties.getProperty("address")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取验证码
     *
     * @param
     * @return void
     * @author yezhendong
     * @date 2020/2/12
     */
    public void getCode() {
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        singleThread.execute(() -> {
            String phone = phoneText.getText();
            if (!checkPhone(phone)) {
                logArea.appendText("手机号错误\n");
                return;
            }
            String authCodeUrl = BASE_URL + codeUrlV2;
            JSONObject body = new JSONObject();
            body.put("phone", phone);
            String result = null;
            while (true) {
                logArea.appendText("开始请求\n");
                if (result != null) {
                    try {
                        logArea.appendText(formatResult(result) + "\n");
                    } catch (Exception e) {
                        logArea.appendText("请求结果转换错误：" + result + "\n");
                    }
                    break;
                }
                try {
                    result = HttpUtil.doPost(authCodeUrl, HEADERS, body.toJSONString());
                }catch (Exception e) {
                    e.printStackTrace();
                    logArea.appendText("请求失败\n");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        singleThread.shutdown();
    }

    /**
     * 开始预约
     *
     * @param
     * @return void
     * @author yezhendong
     * @date 2020/2/12
     */
    public void start() {
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        singleThread.execute(() -> {
            String name = nameText.getText();
            String idNo = idText.getText();
            String phone = phoneText.getText();
            String code = codeText.getText();
            String town = (String) townBox.getValue();
            String address = addressText.getText();

            if (!IdCardUtil.validate(idNo)) {
                logArea.appendText("身份证错误\n");
                return;
            }
            if (!checkPhone(phone)) {
                logArea.appendText("手机号错误\n");
                return;
            }

            if (checkString(name) && checkString(code) && checkString(town) && checkString(address)) {
                String requestUrl = BASE_URL + requestUrlV2;

                String sign = "address=" + address +
                        "&code=" + code +
                        "&id_card=" + idNo +
                        "&name=" + name +
                        "&phone=" + phone +
                        "&town=" + town +
                        "&secret=godlee";
                sign = MD5Util.encrypt(sign);
                JSONObject body = new JSONObject();
                body.put("name", name);
                body.put("phone", phone);
                body.put("id_card", idNo);
                body.put("town", town);
                body.put("code", code);
                body.put("address", address);
                body.put("sign", sign);
                String result = null;
                while (true) {
                    logArea.appendText("开始请求\n");
                    if (result != null) {
                        logArea.appendText(formatResult(result) + "\n");
                        break;
                    }
                    try {
                        result = HttpUtil.doPost(requestUrl, HEADERS, body.toJSONString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        logArea.appendText("请求失败\n");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                logArea.appendText("不能为空\n");
            }
        });
        singleThread.shutdown();
    }

    /**
     * 检查手机号
     *
     * @param phone
     * @return boolean
     * @author yezhendong
     * @date 2020/2/12
     */
    private boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Matcher matcher = phonePattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * 字符串不为空
     *
     * @param s
     * @return boolean
     * @author yezhendong
     * @date 2020/2/12
     */
    private boolean checkString(String s) {
        return s != null && !"".equals(s);
    }

    /**
     * 格式化结果
     *
     * @param result
     * @return java.lang.String
     * @author yezhendong
     * @date 2020/2/12
     */
    private String formatResult(String result) {
        try {
            JSONObject jsonObject = JSON.parseObject(result);
            String errorCode = jsonObject.getString("error_code");
            String success = Boolean.parseBoolean(jsonObject.getString("success")) ? "成功" : "失败";
            String message = jsonObject.getString("message");
            return MessageFormat.format("错误码：{0}，{1}，信息：{2}", errorCode, success, message);
        } catch (Exception e) {
            return result;
        }
    }

    private String formatString(String s) {
        String res = "";
        if (s != null) {
            res = new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        return res;
    }
}
