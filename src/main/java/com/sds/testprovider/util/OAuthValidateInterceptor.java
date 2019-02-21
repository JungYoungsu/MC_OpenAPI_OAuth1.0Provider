package com.sds.testprovider.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.thinker.oauth.parameter.OAuthTokenParam;

import com.sds.testprovider.model.ConsumerVO;
import com.sds.testprovider.model.UsersVO;
import com.sds.testprovider.service.ConsumerService;
import com.sds.testprovider.service.UsersService;

public class OAuthValidateInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private UsersService usersService;

	@Autowired
	private ConsumerService consumerService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		OAuthTokenParam param = new OAuthTokenParam(request);
		long userNo = param.getUserNo();
		String consumerKey = param.getConsumerKey();

		UsersVO usersVO = usersService.selectUserByUserNo(userNo);
		ConsumerVO consumerVO = consumerService.selectByConsumerKey(consumerKey);
		// 토큰 검증
		param.validateRequestToken(consumerVO.getConsumerSecret(), usersVO.getPassword());
		
		// 서블릿에 유저 정보 넘겨주기
		request.setAttribute("user", usersVO);
		System.out.println("#############");
		return true;
	}

}
