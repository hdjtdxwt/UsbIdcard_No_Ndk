package com.epsit.usbidcard_no_ndk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by Administrator on 2018/3/1/001.
 */

public class IdCardInfo implements Parcelable{
    String word_name;
    String word_gender;
    String word_nation;
    String word_birthday;
    String word_address;
    String word_idCard;
    String word_issuingAuthority;
    String word_startTime;
    String word_startopTime;

    public IdCardInfo(){}
    public IdCardInfo(String word_name, String word_gender, String word_nation, String word_birthday, String word_address, String word_idCard, String word_issuingAuthority, String word_startTime, String word_startopTime) {
        this.word_name = word_name;
        this.word_gender = word_gender;
        this.word_nation = word_nation;
        this.word_birthday = word_birthday;
        this.word_address = word_address;
        this.word_idCard = word_idCard;
        this.word_issuingAuthority = word_issuingAuthority;
        this.word_startTime = word_startTime;
        this.word_startopTime = word_startopTime;
    }

    protected IdCardInfo(Parcel in) {
        word_name = in.readString();
        word_gender = in.readString();
        word_nation = in.readString();
        word_birthday = in.readString();
        word_address = in.readString();
        word_idCard = in.readString();
        word_issuingAuthority = in.readString();
        word_startTime = in.readString();
        word_startopTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(word_name);
        dest.writeString(word_gender);
        dest.writeString(word_nation);
        dest.writeString(word_birthday);
        dest.writeString(word_address);
        dest.writeString(word_idCard);
        dest.writeString(word_issuingAuthority);
        dest.writeString(word_startTime);
        dest.writeString(word_startopTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IdCardInfo> CREATOR = new Creator<IdCardInfo>() {
        @Override
        public IdCardInfo createFromParcel(Parcel in) {
            return new IdCardInfo(in);
        }

        @Override
        public IdCardInfo[] newArray(int size) {
            return new IdCardInfo[size];
        }
    };

    @Override
    public String toString() {
        return "IdCardInfo{" +
                "word_name='" + word_name + '\'' +
                ", word_gender='" + word_gender + '\'' +
                ", word_nation='" + word_nation + '\'' +
                ", word_birthday='" + word_birthday + '\'' +
                ", word_address='" + word_address + '\'' +
                ", word_idCard='" + word_idCard + '\'' +
                ", word_issuingAuthority='" + word_issuingAuthority + '\'' +
                ", word_startTime='" + word_startTime + '\'' +
                ", word_startopTime='" + word_startopTime + '\'' +
                '}';
    }
}
