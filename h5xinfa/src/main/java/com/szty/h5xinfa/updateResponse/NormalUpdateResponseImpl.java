package com.szty.h5xinfa.updateResponse;

import android.text.TextUtils;

import com.szty.h5xinfa.model.ConfigBean;
import com.szty.h5xinfa.model.UpdateBean;
import com.szty.h5xinfa.model.UpgradeBean;

/**
 * 响应值为含有竖线的字符串
 */
public class NormalUpdateResponseImpl extends BaseUpdateResponse{

    public NormalUpdateResponseImpl(ConfigBean configBean) {
        super(configBean);
    }

    @Override
    public UpgradeBean getUpgradeBean(String response) {
        UpgradeBean upgradeBean = new UpgradeBean();
        try {
            if (!TextUtils.isEmpty(response) && response.contains("|")) {
                // 132|/Upload/edition/InformationDisplay_20191221141046221.zip
                String strs[] = response.split("\\|");
                if (strs.length == 2) {
                    String idStr = strs[0];
                    String pathStr = strs[1];
                    upgradeBean.setId(Integer.valueOf(idStr));
                    upgradeBean.setFilepath(pathStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return upgradeBean;
    }




    @Override
    public UpdateBean getUpdateBean(String response) {
        UpdateBean updateBean = new UpdateBean();
        try {
            if (!TextUtils.isEmpty(response) && response.contains("|")) {
                // 132|/Upload/edition/InformationDisplay_20191221141046221.zip
                String strs[] = response.split("\\|");
                if (strs.length == 3) {
                    String idStr = strs[0];
                    String pathStr = strs[1];
                    String length = strs[2];
                    updateBean.setId(Integer.valueOf(idStr));
                    updateBean.setFilepath(pathStr);
                    updateBean.setLength(Integer.valueOf(length));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateBean;
    }
}
