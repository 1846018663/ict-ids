package com.hnu.ict.ids.utils;

import com.alibaba.fastjson.JSON;
import com.hnu.common.respone.BaseResponse;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class CheckParamsInterceptor extends HandlerInterceptorAdapter  {
    private static Logger LOG = LoggerFactory.getLogger(CheckParamsInterceptor.class);

    //在请求处理之前进行调用（Controller方法调用之前
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        //如果不是映射到方法直接通过
        if (!(o instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) o;
        Method method = handlerMethod.getMethod();
        if (method.getAnnotation(ParamsNotNull.class) != null) {
            ParamsNotNull noNullAnnotation = method.getAnnotation(ParamsNotNull.class);
            String Str = noNullAnnotation.str();

            String[] res = Str.split(",");
            boolean bool=true;
            for (int i = 0; i < res.length; i++) {
                //从httpServletRequest获取注解上指定的参数
                Object obj = httpServletRequest.getParameter(res[i]);
                if(obj==null){
                    bool=false;
                    break;
                }
            }

            if (bool) {
                return true;
            } else {
                PojoBaseResponse result = new PojoBaseResponse();
                result.setErrorCode(ResutlMessage.FAIL.getName());
                result.setErrorMessage("参数不完整");
                result.setStatus(BaseResponse.Status.FAILED);
                httpServletResponse.setHeader("content-type", "text/html;charset=utf-8");
                httpServletResponse.getWriter().write(JSON.toJSONString(result));
                return false;
            }
        }else{
            return true;
        }
    }
    /**
     * 拿到在参数上加了该注解的参数名称
     */
    private List getParamsName(HandlerMethod handlerMethod) {
        Parameter[] parameters = handlerMethod.getMethod().getParameters();
        List<String> list = new ArrayList<String>();
        for (Parameter parameter : parameters) {
            if(parameter.isAnnotationPresent(ParamsNotNull.class)){
                list.add(parameter.getName());
            }
        }
        return list;
    }


    //请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

            //在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
      @Override
     public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {


    }
}