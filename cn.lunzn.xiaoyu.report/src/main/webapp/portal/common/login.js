$(function(){
	$("#goHome").click(function(){
		goHome();
	});
	
	// 企业微信登录
	$("#qywxLogin").click(function(){
		window.location.href= "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=wwc38d087779e08e90&agentid=1000002&redirect_uri=http%3a%2f%2foa.lunzn.com%3a6060%2fszzz%2fstaff%2falogin&state=web_login@gyoss9";
	});
	
	var checkout1 = new AKCheckout();
	checkout1.addField({name:'employeeId', isRequired:true, emptyMsg:"必填项"});
	checkout1.addField({name:'name', isRequired:true, emptyMsg:"必填项"});
	checkout1.addField({name:'roleId', isRequired:true, emptyMsg:"必填项"});
	//checkout1.addField({name:'supervisor', isRequired:true, emptyMsg:"必填项"});
	
	// 登录按钮事件
	$("#loginBtn").click(function(){
		 if(!checkout1.validate()){
			 return;
		 }
		 
		// 获取参数
		var dataParam = {
				employeeId:$("input[name='employeeId']").val(), 
				name:$("input[name='name']").val(),
				roleId:$("input[name='roleId']").val(),
				supervisorName:$("input[name='supervisorName']").val(),
				supervisor:$("input[name='supervisor']").val()};
		
		var param = {data:dataParam, url:"/staff/tlogin", callback:function(response){
			if(response.code == 0){
				// var userSession = JSON.parse(sessionStorage.getItem("userSession"));
				/*var userSession = {};
				userSession["isLogin"] = true;
				// 设置用户信息
				$.extend(userSession, response.data);
				// 会话存储
				sessionStorage.setItem("userSession", JSON.stringify(userSession));*/
				// 跳转
				goHome();
			}else{
				$("#errorTip").text($.i18n.prop(response.code));
			}
		}};
		// 登录
		$.myajax(param);
	});
	
	$(document).unbind("keydown").bind("keydown", function(event){
		if(event.keyCode==13){
			$("#loginBtn").click(); 
		} 
	});
});