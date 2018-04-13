/**
 * 设置页面头部菜单<br>
 * 请保证1、2的顺序，其它随意
 */
$(function(){
	// 1、生成菜单
	buildHeadMenu();
	
	// 2、绑定导航菜单事件
	bindNavEvent();
	
	// n、渲染已登录的员工信息
	autoLoginConfirm();
	// renderLogin();
});

/**
 * 登录自动确认
 */
function autoLoginConfirm(){
	var param = {data:{}, url:"/staff/auto", isLoading:false, callback:function(response){
		if(!$.isEmptyObject(response)){
			var userSession = {};
			userSession["isLogin"] = true;
			// 设置用户信息
			$.extend(userSession, response);
			// 会话存储
			sessionStorage.setItem("userSession", JSON.stringify(userSession));
			// 渲染
			renderLogin();
		}
	}};
	$.myajax(param);
}

/**
 * 生成菜单
 */
function buildHeadMenu(){
    var headMenuHtml = [];
	
	headMenuHtml.push('<nav class="navbar navbar-default navbar-fixed-top navbar-app" >');
	headMenuHtml.push('<div class="container">');
	headMenuHtml.push('<div class="col-lg-12">');
	headMenuHtml.push('<div class="navbar-header">');
	headMenuHtml.push('<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">');
	headMenuHtml.push('<span class="sr-only">&nbsp;</span>');
	headMenuHtml.push('<span class="icon-bar"></span>');
	headMenuHtml.push('<span class="icon-bar"></span>');
	headMenuHtml.push('<span class="icon-bar"></span>');
	headMenuHtml.push('</button>');
	headMenuHtml.push('<a class="navbar-brand" id="homePage" href="#"> <img src="'+ rootPath +'/theme/images/company/gs_1.png" style="padding-top: 4px;">'+""+'</a>');
	headMenuHtml.push('</div>');
	
	headMenuHtml.push('<div class="navbar-collapse navbar-right collapse" aria-expanded="false" style="height: 1px;">');
	// 菜单
	headMenuHtml.push('<ul class="nav navbar-nav" id="nav_menu">');
	headMenuHtml.push('<li><a href="#" id="nav_accounting">'+$.i18n.prop("sz")+'</a></li>');
	headMenuHtml.push('<li><a href="#" id="nav_worker">'+$.i18n.prop("zz")+'</a></li>');
	headMenuHtml.push('<li><a href="#" id="nav_audit">'+$.i18n.prop("let_me_check")+'</a></li>');
	headMenuHtml.push('<li><a href="#" id="nav_login">'+$.i18n.prop("login")+'</a></li>');
	headMenuHtml.push('<li><a href="#" id="nav_loginout" style="display: none;">'+$.i18n.prop("login_out")+'</a></li>');
	headMenuHtml.push('</ul>');
	
	headMenuHtml.push('</div>');
	headMenuHtml.push('</div>');
	headMenuHtml.push('</div>');
	headMenuHtml.push('</nav>');
	
	$("body").prepend(headMenuHtml.join(" "));
}

/**
 * 绑定导航菜单事件
 */
function bindNavEvent(){
	// 菜单
	$("#homePage").attr("href", rootPath+"/portal/home.html");
	$("#nav_accounting").attr("href", rootPath+"/portal/reportwork/reportList.html");
	$("#nav_worker").attr("href",rootPath+"/portal/zz/zzlist.html");
	//$("#nav_about").attr("href",rootPath+"/portal/common/about.html");
	
	$("#nav_audit").attr("href", rootPath+"/portal/auditflow.html");
	
//	$("#nav_login").attr("href",rootPath+"/portal/common/login.html");
	$("#nav_login").click(function(){
		//上线时该url要根据实际参数修改
		window.location.href= "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=wwc38d087779e08e90&agentid=1000002&redirect_uri=http%3a%2f%2foa.lunzn.com%3a6060%2fszzz%2fstaff%2falogin&state=web_login@gyoss9";
	});
	$("#nav_loginout").click(function(){
		// 请求后台注销当前会话，不显示遮罩层
		var param = {data:{}, url:"/staff/tlogout", isLoading:false, callback:function(response){
			if(response.code == 0){
				sessionStorage.clear();
				// 重新渲染
				renderLogin();
				// 跳转到登录页面
//				window.location.href = rootPath + "/portal/common/login.html";
			}
		}};
		$.myajax(param);
	});
}

/**
 * 渲染已登录员工信息
 */
function renderLogin(){
	
	$("#nav_audit").find("span").remove();
	// 用户信息移除
	$("#nav_userinfo").remove();
	// 未登录，显示登录
	$("#nav_login").show();
	// 未登录，隐藏注销
	$("#nav_loginout").hide();
	
	if(sessionStorage.getItem("userSession")){
		$("#nav_login").hide();
		$("#nav_loginout").show();
		
		var userSession = JSON.parse(sessionStorage.getItem("userSession"));
		var ulel = [];
		ulel.push('<li><a href="#" id="nav_userinfo">');
		ulel.push(userSession.name);
		ulel.push('</a></li>');
		
		$("#nav_loginout").parent().before(ulel.join(" "));
		
		// 渲染待审核数据
		var param = {data:{handleFlag:Constant.handleFlag.handle_no, staffId:userSession.employeeId}, url:"/auditflow/querytotal", isLoading:false, callback:function(response){
			if(response.code == 0){
				var badgeNum = response.total;
				if(badgeNum > 99){
					badgeNum = '99+';
				}
				if(response.total > 0){
					$("#nav_audit").append(' <span style="margin-bottom:3px;" class="badge">'+badgeNum+'</span>');
				}
			}
		}};
		$.myajax(param);
	}
}