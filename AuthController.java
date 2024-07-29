package jp.co.internous.team2405.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.team2405.model.domain.MstUser;
import jp.co.internous.team2405.model.form.UserForm;
import jp.co.internous.team2405.model.mapper.MstUserMapper;
import jp.co.internous.team2405.model.mapper.TblCartMapper;
import jp.co.internous.team2405.model.session.LoginSession;

/**
 * 認証に関する処理を行うコントローラー
 * @author インターノウス
 *
 */
@RestController
@RequestMapping("/team2405/auth")
public class AuthController {

	/*
	 * フィールド定義
	 */
	@Autowired
	private LoginSession loginSession;

	@Autowired
	private MstUserMapper mstUserMapper;

	@Autowired
	private TblCartMapper tblCartMapper;

	private Gson gson = new Gson();

	/**
	 * ログイン処理をおこなう
	 * @param f ユーザーフォーム
	 * @return ログインしたユーザー情報(JSON形式)
	 */
	@PostMapping("/login")
	public String login(@RequestBody UserForm f) {
		
		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		
		int tmpUserId = loginSession.getTmpUserId();
		if (user != null && tmpUserId != 0) {
			int count = tblCartMapper.findCountByUserId(tmpUserId);
			if (count > 0) {
				tblCartMapper.updateUserId(user.getId(), tmpUserId);
			}
		}
		
		if (user != null) {
			loginSession.setUserId(user.getId());
			loginSession.setTmpUserId(0);
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
			loginSession.setLogined(true);
		} else {
			loginSession.setUserId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);
			loginSession.setLogined(false);
		}
		
		return gson.toJson(user);
		
	}

	/**
	 * ログアウト処理をおこなう
	 * @return 空文字
	 */
	@PostMapping("/logout")
	public String logout() {
		
		loginSession.setUserId(0);
		loginSession.setTmpUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLogined(false);
		
		return "";
		
	}

	/**
	 * パスワード再設定をおこなう
	 * @param f ユーザーフォーム
	 * @return 処理後のメッセージ
	 */
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {

		String newPassword = f.getNewPassword();
		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());

		if (user.getPassword().equals(newPassword)) {
			return "現在のパスワードと同一文字列が入力されました。";
		}

		mstUserMapper.updatePassword(user.getUserName(), newPassword);
		loginSession.setPassword(newPassword);
		
		return "パスワードが再設定されました。";

	}
}
