package com.dianping.puma.biz.service.remote;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientTokenDao;
import com.dianping.puma.biz.entity.ClientTokenEntity;
import com.dianping.puma.server.model.ClientToken;
import com.dianping.puma.server.service.PumaClientTokenService;

/**
 * Created by xiaotian.li on 16/3/31.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientTokenService implements PumaClientTokenService {

    private Converter converter;

    private ClientTokenDao clientTokenDao;

    @Override
    public int update(String clientName, ClientToken clientToken) {
        ClientTokenEntity entity = converter.convert(clientToken, ClientTokenEntity.class);
        entity.setClientName(clientName);
        return clientTokenDao.update(entity);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientTokenDao(ClientTokenDao clientTokenDao) {
        this.clientTokenDao = clientTokenDao;
    }
}