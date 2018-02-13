// ISecurityCenter.aidl
package com.innovator.ipcclient;

// 加解密功能

interface ISecurityCenter {

    String encrypt(String content);
    String decript(String password);
}
