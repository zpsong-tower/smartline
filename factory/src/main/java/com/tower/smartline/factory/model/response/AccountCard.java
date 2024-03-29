package com.tower.smartline.factory.model.response;

import androidx.annotation.NonNull;

/**
 * AccountRspModel
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/6/9 6:41
 */
public class AccountCard {
    // UserCard
    private UserCard userCard;

    // Token
    private String token;

    // 是否已绑定PushId
    private boolean isBind;

    public UserCard getUserCard() {
        return userCard;
    }

    public void setUserCard(UserCard userCard) {
        this.userCard = userCard;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    @NonNull
    @Override
    public String toString() {
        return "AccountRspModel{" +
                "userCard=" + userCard +
                ", token='" + token + '\'' +
                ", isBind=" + isBind +
                '}';
    }
}
