package jp.co.internous.react.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.react.model.domain.MstDestination;
import jp.co.internous.react.model.mapper.MstDestinationMapper;
import jp.co.internous.react.model.mapper.TblCartMapper;
import jp.co.internous.react.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.react.model.session.LoginSession;

@Controller
@RequestMapping("/react/settlement")
public class SettlementController {
	
	@Autowired
	MstDestinationMapper mstDestinationMapper;
	
	@Autowired
	protected LoginSession loginSession;
	
	private Gson gson = new Gson();
	
	@Autowired
	TblCartMapper tblCartMapper;
	
	@Autowired
	TblPurchaseHistoryMapper tblPurchaseHistoryMapper;
	
	/*userID long型 int型*/
	@RequestMapping("/")
	public String index(Model m) {
		long userId = loginSession.getUserId();
		
	/*mst_destinationテーブルからユーザーの宛先情報を取得する ※開発予習sessionテキスト*/
		List<MstDestination> destinations = mstDestinationMapper.findByUserId((int) userId);
		m.addAttribute("loginSession", loginSession);
		m.addAttribute("destinations", destinations);
		
		return "settlement";
		
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/complete")
	@ResponseBody
	public boolean complete(@RequestBody String destinationId) {
		
		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		String id = map.get("destinationId");
		
		//購入履歴テーブルに商品ごとの決済情報を登録。カートテーブルの情報を表品購入履歴テーブルにinsertする
		long userId = loginSession.getUserId();
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("destinationId", id);
		parameter.put("userId", userId);
		int insertCount = tblPurchaseHistoryMapper.insert(parameter);
		
		//ユーザーのDBカート情報テーブルの情報を削除。登録成功した場合Tbl_CartMapperを呼び出す
		long deleteCount = 0;
		if (insertCount > 0) {
			deleteCount = tblCartMapper.deleteByUserId(userId);
		}
		
		//カート情報の削除後、購入履歴画面に遷移 return  insert件数 == delete件数
		return deleteCount == insertCount;
	}
	
}
