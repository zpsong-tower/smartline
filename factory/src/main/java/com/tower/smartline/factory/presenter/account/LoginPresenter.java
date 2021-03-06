package com.tower.smartline.factory.presenter.account;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tower.smartline.common.Constants;
import com.tower.smartline.factory.R;
import com.tower.smartline.factory.data.IDataSource;
import com.tower.smartline.factory.data.helper.AccountHelper;
import com.tower.smartline.factory.model.api.account.LoginModel;
import com.tower.smartline.factory.model.api.account.RegisterModel;
import com.tower.smartline.factory.model.response.UserCard;
import com.tower.smartline.factory.presenter.BasePresenter;

import net.qiujuer.genius.kit.handler.Run;

/**
 * 登录Presenter
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/6/8 0:54
 */
public class LoginPresenter extends BasePresenter<ILoginContract.View>
        implements ILoginContract.Presenter, IDataSource.Callback<UserCard> {
    /**
     * 构造方法
     *
     * @param view Presenter需要绑定的View层实例
     */
    public LoginPresenter(@NonNull ILoginContract.View view) {
        super(view);
    }

    @Override
    public void login(String phone, String password) {
        start();
        if (getView() == null || !checkString(phone, password)) {
            return;
        }
        LoginModel model = new LoginModel(phone, password);
        AccountHelper.login(model, this);
    }

    @Override
    public void register(String phone, String password, String username) {
        start();
        if (getView() == null || !checkString(phone, password) || !checkString(username)) {
            return;
        }
        RegisterModel model = new RegisterModel(phone, password, username);
        AccountHelper.register(model, this);
    }

    @Override
    public boolean checkString(String phone, String password) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            // 手机号或密码为空
            getView().showError(R.string.toast_app_account_parameters_empty);
            return false;
        }
        if (!phone.matches(Constants.REGEX_PHONE)) {
            // 手机号非法
            getView().showError(R.string.toast_app_account_invalid_parameter_phone);
            return false;
        }
        if (!password.matches(Constants.REGEX_PASSWORD)) {
            // 密码非法
            getView().showError(R.string.toast_app_account_invalid_parameter_password);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkString(String username) {
        if (TextUtils.isEmpty(username)) {
            // 用户名为空
            getView().showError(R.string.toast_app_account_parameter_empty);
            return false;
        }
        if (!username.matches(Constants.REGEX_USERNAME)) {
            // 用户名非法
            getView().showError(R.string.toast_app_account_invalid_parameter_username);
            return false;
        }
        return true;
    }

    @Override
    public void onSuccess(UserCard userCard) {
        Run.onUiAsync(() -> {
            if (getView() != null) {
                getView().submitSuccess();
            }
        });
    }

    @Override
    public void onFailure(int strRes) {
        Run.onUiAsync(() -> {
            if (getView() != null) {
                getView().showError(strRes);
            }
        });
    }
}
