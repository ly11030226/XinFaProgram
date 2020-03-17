package com.szty.h5xinfa.updateResponse;

import com.google.gson.Gson;
import com.szty.h5xinfa.model.ConfigBean;
import com.szty.h5xinfa.model.UpdateBean;
import com.szty.h5xinfa.model.UpgradeBean;

public class JsonUpdateResponseImpl extends BaseUpdateResponse{

    public JsonUpdateResponseImpl(ConfigBean configBean) {
        super(configBean);
    }

    @Override
    public UpgradeBean getUpgradeBean(String response) {
        Gson gson = new Gson();
        UpgradeBean  upgradeBean = gson.fromJson(response,UpgradeBean.class);
        return upgradeBean;
    }

    @Override
    public UpdateBean getUpdateBean(String response) {
        Gson gson = new Gson();
        UpdateBean updateBean = gson.fromJson(response, UpdateBean.class);
        return updateBean;
    }
}
