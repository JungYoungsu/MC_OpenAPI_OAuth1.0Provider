package com.sds.testprovider.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.thinker.oauth.generator.TokenGenerator;
import org.thinker.oauth.parameter.AccessTokenParam;
import org.thinker.oauth.util.OAuthMsgConstants;

import com.sds.testprovider.model.AccessTokenVO;
import com.sds.testprovider.model.ConsumerVO;
import com.sds.testprovider.model.RequestTokenVO;
import com.sds.testprovider.model.UsersVO;
import com.sds.testprovider.service.ConsumerService;
import com.sds.testprovider.service.RequestTokenService;
import com.sds.testprovider.service.UsersService;

@Controller
public class AccessTokenController {

	@Autowired
	private RequestTokenService requestTokenService;

	@Autowired
	private UsersService usersService;

	@Autowired
	private ConsumerService consumerService;

	@RequestMapping(value = "access_token")
	public ModelAndView getAccessToken(HttpServletRequest request) throws Exception {
		// 아래 return 문을 주석 처리하고 코드를 작성합니다.

		// 1. 파라미터 파싱
		AccessTokenParam param = new AccessTokenParam(request);
		// 1.1 DB테이블에서 consumer, requestToken, User 정보 읽음
		// ConsumerSecret, Password, Verifier
		ConsumerVO consumerVO = consumerService.selectByConsumerKey(param.getConsumerKey());
		RequestTokenVO requestTokenVO = requestTokenService.getRequestToken(param.getRequestToken());
		UsersVO usersVO = usersService.selectUserByUserNo(requestTokenVO.getUserNo());

		ModelAndView mav = new ModelAndView();
		mav.setViewName("access_token");

		try { // 2.Signature Validation!! 유효하지 않으면 예외 발생!
			param.validateRequestToken(requestTokenVO.getRequestTokenSecret(), requestTokenVO.getVerifier(),
					consumerVO.getConsumerSecret());
			// 2.1 유효하다면 RequestToken테이블의 레코드 삭제 : 임시 토큰이기 때문에
			requestTokenService.deleteRequestToken(requestTokenVO.getRequestToken());

			// 3. AccessToken 생성
			AccessTokenVO accessTokenVO = TokenGenerator.generateAccessToken(usersVO, consumerVO);

			StringBuilder sb = new StringBuilder();
			sb.append(OAuthMsgConstants.OAUTH_TOKEN + "=" + accessTokenVO.getAccessToken() + "&");
			sb.append(OAuthMsgConstants.OAUTH_TOKEN_SECRET + "=" + accessTokenVO.getAccessTokenSecret() + "&");
			sb.append("userno=" + accessTokenVO.getUserNo() + "&");
			sb.append("userid=" + accessTokenVO.getUserID());

			mav.addObject("isValid", true);
			mav.addObject("message", sb.toString());
		} catch (Exception e) {
			mav.addObject("isValid", false);
			mav.addObject("message", e.getMessage());
		}
		return mav;
	}
}
