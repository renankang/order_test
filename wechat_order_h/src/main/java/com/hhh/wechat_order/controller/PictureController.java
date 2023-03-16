package com.hhh.wechat_order.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hhh.wechat_order.VO.ResultVo;
import com.hhh.wechat_order.entity.Picture;
import com.hhh.wechat_order.exception.SellException;
import com.hhh.wechat_order.form.PictureForm;
import com.hhh.wechat_order.repository.PictureRepository;
import com.hhh.wechat_order.utils.ResultVOUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信用户端的图片相关操作的Controller
 * @author HHH
 * @version 1.0 2019/10/17
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Autowired
    private PictureRepository pictureRepository;

    @GetMapping("/getAll")
    public ResultVo getUserInfo(){
        List<Picture> pictures = pictureRepository.findAll();
        String flat= "https://images.weserv.nl/?url=";
        List<String> imageListOld = pictures.stream().map(Picture::getPicUrl).collect(Collectors.toList());
        List<String> imageListNew = new ArrayList<>();
        for (String s : imageListOld) {
            imageListNew.add(flat+s);
        }
        return ResultVOUtil.success(imageListNew);
    }

    //页面相关
    //TODO
    @GetMapping("/list")
    public ModelAndView list(Map<String , Object> map){
        List<Picture> pictures = pictureRepository.findAll();
        map.put("pictures" , pictures);
        return new ModelAndView("picture/list" , map);
    }

    @GetMapping("/index")
    public ModelAndView index(@RequestParam(value = "picId" , required = false) Integer picId ,
                              Map<String , Object> map){
        if(picId != null){
            Picture picture = pictureRepository.findByPicId(picId);
            map.put("picture" , picture);
        }
        return new ModelAndView("picture/index" , map);
    }

    @PostMapping("/save")
    public ModelAndView save(@Valid PictureForm form ,
                             BindingResult bindingResult ,
                             Map<String , Object> map){
        if(bindingResult.hasErrors()){
            map.put("msg" , bindingResult.getFieldError().getDefaultMessage());
            map.put("url" , "/sell/picture/index");
            return new ModelAndView("common/error" , map);
        }
        Picture picture = new Picture();
        try{
            if(form.getPicId() != null){
                picture = pictureRepository.findByPicId(form.getPicId());
            }
            BeanUtils.copyProperties(form , picture);
            pictureRepository.save(picture);
        }catch (SellException e){
            map.put("msg" , e.getMessage());
            map.put("url" , "/sell/picture/index");
            return new ModelAndView("common/error" , map);
        }
        map.put("msg" , "操作成功！");
        map.put("url", "/sell/picture/list");
        return new ModelAndView("common/success", map);
    }

    @GetMapping("/delete")
    public ModelAndView delete(@RequestParam("picId") Integer picId , Map<String , Object> map){
        Picture picture = pictureRepository.findByPicId(picId);
        pictureRepository.delete(picture);
        map.put("msg" , "图片删除成功！");
        map.put("url" , "/sell/picture/list");
        return new ModelAndView("common/success" , map);
    }
    @RequestMapping("/testopenid")
    public String getUserInfo(@RequestParam(name = "code") String code) throws Exception {
        System.out.println("code" + code);
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        url += "?appid=wx222142ebe6d03a42";//自己的appid
        url += "&secret=191b8cac87d40d19834efc0ed17149a8";//自己的appSecret
        url += "&js_code=" + code;
        url += "&grant_type=authorization_code";
        url += "&connect_redirect=1";
        String res = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);    //GET方式
        CloseableHttpResponse response = null;
        // 配置信息
        RequestConfig requestConfig = RequestConfig.custom()          // 设置连接超时时间(单位毫秒)
                .setConnectTimeout(5000)                    // 设置请求超时时间(单位毫秒)
                .setConnectionRequestTimeout(5000)             // socket读写超时时间(单位毫秒)
                .setSocketTimeout(5000)                    // 设置是否允许重定向(默认为true)
                .setRedirectsEnabled(false).build();           // 将上面的配置信息 运用到这个Get请求里
        httpget.setConfig(requestConfig);                         // 由客户端执行(发送)Get请求
        response = httpClient.execute(httpget);                   // 从响应模型中获取响应实体
        HttpEntity responseEntity = response.getEntity();
        System.out.println("响应状态为:" + response.getStatusLine());
        if (responseEntity != null) {
            res = EntityUtils.toString(responseEntity);
            System.out.println("响应内容长度为:" + responseEntity.getContentLength());
            System.out.println("响应内容为:" + res);
        }
        // 释放资源
        if (httpClient != null) {
            httpClient.close();
        }
        if (response != null) {
            response.close();
        }
        JSONObject jo = JSON.parseObject(res);
        String openid = jo.getString("openid");
        System.out.println("openid" + openid);
        return openid;
    }
}
