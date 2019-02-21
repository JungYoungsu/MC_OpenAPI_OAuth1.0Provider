package com.sds.testprovider.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.sds.testprovider.model.RequestTokenVO;
import com.sds.testprovider.model.UsersVO;
import com.sds.testprovider.service.RequestTokenService;
import com.sds.testprovider.service.UsersService;
import com.sds.testprovider.util.SessionUtil;

@Controller
@RequestMapping(value = "/authorize")
public class AuthorizeController {

	@Autowired
	private RequestTokenService requestTokenService;

	@Autowired
	private UsersService usersService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView authorizeGet(HttpServletRequest request) throws Exception {
		// 아래 return 문을 주석 처리하고 코드를 작성합니다.
		ModelAndView mav = new ModelAndView();
		String oauth_token = (String) request.getParameter("oauth_token");
		if (oauth_token != null) {
			// 1. oauth_token 값으로 tbl_request_token 조회하여 레코드가 존재하지 않으면 오류
			RequestTokenVO requestTokenVO = requestTokenService.getRequestToken(oauth_token);
			if (requestTokenVO != null) {
				mav.setViewName("authorize"); // Authorize 할거냐? 묻는 JSP 파일
				mav.addObject("requestTokenVO", requestTokenVO);
			}
		} else {
			mav.setViewName("authorize_error");
			mav.addObject("errorMessage", "invalid oauth_token!");
		}
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView authorizePost(HttpSession session, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// 아래 return 문을 주석 처리하고 코드를 작성합니다.
		// 1. QueryString 값 파싱
		String allow_deny = request.getParameter("allow_deny");
		String oauth_token = request.getParameter("oauth_token");
		String userid = request.getParameter("userid");
		String password = request.getParameter("password");

		// 2. 임시 생성된 RequestToken 값 읽어오기 (from tbl_request_token)
		RequestTokenVO tokenVO = (RequestTokenVO) requestTokenService.getRequestToken(oauth_token);
		ModelAndView mav = new ModelAndView();
		if (tokenVO == null) {
			mav.setViewName("authroize_error");
			mav.addObject("errorMessage", "Invalid Token");
		}
		mav.addObject("requestTokenVO", tokenVO);
		mav.setViewName("authorize");
		if (allow_deny.equals("allow")) { // 앱 승인
			UsersVO usersVO = null;
			if (!SessionUtil.isLoginned(session)) { // 로그인 처리
				UsersVO inputVO = new UsersVO(userid, password, "", 0);
				usersVO = usersService.selectUsers(inputVO);
				if (usersVO != null) {
					SessionUtil.loginUser(session, usersVO);
				} else { // 승인 버튼을 눌렀으나 유저 ID PWD 가 올바르지 않아 다시 authorize 페이지로 이동
					mav.addObject("loginResult", "false");
					mav.setViewName("authorize");
					return mav;
				}
			}
			// RequestToken Table의 UserNO 필드값을 이제 앱 접근을 허용한 사용자의 UserNO로 UPDATE
			tokenVO.setUserNo(SessionUtil.getUserInfo(session).getUserno());
			requestTokenService.updateUserNo(tokenVO);
			// 로그인된 상태에서 앱을 승인하면 콜백 URL로 이동
			response.sendRedirect(tokenVO.getCallback() + "?oauth_token=" + tokenVO.getRequestToken()
					+ "&oauth_verifier=" + tokenVO.getVerifier());
		} else { // 앱 거부
			// 거부했다면 임시 생성한 RequestToken값을 삭제 후, 승인거부 화면 출력
			requestTokenService.deleteRequestToken(oauth_token);
			mav.setViewName("authorize_error");
			mav.addObject("errorMessage", "User does not allow access to app");
			SessionUtil.logoutUser(session);
		}
		return mav;
	}

}
