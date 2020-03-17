package com.szty.h5xinfa.updateResponse;

import android.text.TextUtils;

import com.szty.h5xinfa.Constant;
import com.szty.h5xinfa.model.ConfigBean;
import com.szty.h5xinfa.model.UpdateBean;
import com.szty.h5xinfa.model.UpgradeBean;

/**
 * 更新文件响应的抽象类
 */
public abstract class BaseUpdateResponse {

    private ConfigBean configBean;
    public BaseUpdateResponse(ConfigBean configBean) {
        this.configBean = configBean;
    }

    /**
     * 通过响应得到String获取下载zip的url
     * @param response
     * @return
     */
    public abstract UpgradeBean getUpgradeBean(String response);
    public abstract UpdateBean getUpdateBean(String response);

    public String getUrl(String filePath){
        String result = "";
        String downloadUrl = getServerIpAndPort();
        if (!TextUtils.isEmpty(downloadUrl) && !TextUtils.isEmpty(filePath)) {
            result = downloadUrl+ filePath;
        }
        return result;
    }



    private String getServerIpAndPort() {
        String result = "";
        if (configBean != null) {
            result = Constant.HTTP_CODE + configBean.getServerIp() + ":" + configBean.getServerPort();
        }
        return result;
    }


}
