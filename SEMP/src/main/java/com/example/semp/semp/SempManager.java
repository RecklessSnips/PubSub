package com.example.semp.semp;

import com.google.gson.Gson;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.MsgVpnApi;
import io.swagger.client.model.SempError;
import io.swagger.client.model.SempMetaOnlyResponse;

/*
    这个类是根据 Solace Swagger SPEC 文件生成的，具体使用可以看记事本 Notes 里的
    Solace Tips 的详细过程
    (因为这个文件里的 API 都是现场生成的 SDK)
 */
public class SempManager {

    private final ApiClient apiClient;
    private final MsgVpnApi msgVpnApi;
    private String URI;
    private String username;
    private String password;
    private String msgVpnName;
    private String queueName;

    public SempManager(String URI, String username, String password, String msgVpnName, String queueName) {
        this.apiClient = new ApiClient();

        this.URI = URI;
        this.username = username;
        this.password = password;
        this.msgVpnName = msgVpnName;
        this.queueName = queueName;

        this.apiClient.setBasePath(URI);
        this.apiClient.setUsername(username);
        this.apiClient.setPassword(password);

        this.msgVpnApi = new MsgVpnApi(apiClient);
    }

    // Get the total number Queued messages
    public int getQueuedMsg() {
        try {
            return msgVpnApi.getMsgVpnQueue(
                    msgVpnName,
                    queueName,
                    null
            ).getCollections().getMsgs().getCount().intValue();
        } catch (ApiException e) {
            handleError(e);
            return -1;
        }
    }

    // Private function to handle errors
    private static void handleError(ApiException ae) {
        Gson gson = new Gson();
        String responseString = ae.getResponseBody();
        SempMetaOnlyResponse respObj = gson.fromJson(responseString, SempMetaOnlyResponse.class);
        SempError errorInfo = respObj.getMeta().getError();
        System.out.println("Error during operation. Details..." +
                "\nHTTP Status Code: " + ae.getCode() +
                "\nSEMP Error Code: " + errorInfo.getCode() +
                "\nSEMP Error Status: " + errorInfo.getStatus() +
                "\nSEMP Error Descriptions: " + errorInfo.getDescription());
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMsgVpnName() {
        return msgVpnName;
    }

    public void setMsgVpnName(String msgVpnName) {
        this.msgVpnName = msgVpnName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public String toString() {
        return "SempManager{" +
                "apiClient=" + apiClient +
                ", msgVpnApi=" + msgVpnApi +
                ", URI='" + URI + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", msgVpnName='" + msgVpnName + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
