package com.xiaoniucode.etp.server.web.serivce;

import com.xiaoniucode.etp.server.web.core.orm.transaction.JdbcTransactionTemplate;
import com.xiaoniucode.etp.server.web.core.server.BizException;
import com.xiaoniucode.etp.server.web.dao.DaoFactory;
import com.xiaoniucode.etp.server.web.common.DigestUtil;
import com.xiaoniucode.etp.server.web.manager.CaptchaManager;
import org.json.JSONObject;

public class UserService {
    private static final JdbcTransactionTemplate TX = new JdbcTransactionTemplate();

    public JSONObject login(JSONObject req) {
        String captchaId = req.optString("captchaId");
        String code = req.optString("code");
        if (!CaptchaManager.verifyAndRemove(captchaId, code)) {
            throw new BizException("验证码过期");
        }
        String username = req.optString("username");
        String password = req.optString("password");
        JSONObject user = DaoFactory.INSTANCE.getUserDao().getByUsername(username);
        if (user == null) {
            throw new BizException(401, "用户不存在！");
        }
        //检查密码
        if (!DigestUtil.encode(password, username).equals(user.optString("password"))) {
            throw new BizException(401, "密码错误");
        }
        //创建登录令牌
        return ServiceFactory.INSTANCE.getAuthTokenService().createToken(user.getInt("id"), username);
    }

    public JSONObject getByUsername(String username) {
        return DaoFactory.INSTANCE.getUserDao().getByUsername(username);
    }

    public void register(JSONObject user) {
        DaoFactory.INSTANCE.getUserDao().insert(user);
    }

    public void deleteAll() {
        DaoFactory.INSTANCE.getUserDao().deleteAll();
    }

    public void updatePassword(Integer userId, JSONObject req) {
        TX.execute(() -> {
            int id = req.getInt("userId");
            String oldPassword = req.getString("oldPassword");
            String newPassword = req.getString("password");
            if (userId != id) {
                throw new BizException(401, "未登录");
            }
            JSONObject user = DaoFactory.INSTANCE.getUserDao().getById(userId);
            String username = user.getString("username");
            if (!user.getString("password").equals(DigestUtil.encode(oldPassword, username))) {
                throw new BizException("原密码不正确");
            }
            DaoFactory.INSTANCE.getUserDao().updatePassword(id, DigestUtil.encode(newPassword, username));
            return null;
        });

    }
}
