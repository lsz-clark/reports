$(function(){
	/*// 从这里开始
	$("#startHere").click(function(){
		window.location.href = rootPath + "/portal/accounting/record.html?type=" + Constant.accountType.income;
	});*/
	
	// 语言切换
	$("#languageBtn").next().find("a").click(function(){
		var htext = $(this).text();
		$("#languageBtnText").text(htext);
		
		// 设置到会话中
		localStorage.setItem("language", $(this).attr("ivalue"));
		
		location.reload();
	});
	
	// 语言切换后显示
	if(localStorage.getItem("language") && localStorage.getItem("language") != "undefined"){
		var ivalue = localStorage.getItem("language");
		$("#languageBtnText").text($("#languageBtn").next().find('a[ivalue="'+ ivalue +'"]').text());
	}
	
	/*// 验证员工是否登录
	$.myajax({data:{}, url:"/staff/auto", isLoading:false, callback:function(responseStaff){
		if(!$.isEmptyObject(responseStaff)){
			
		}else{
			$("#szNotFound").show();
			$("#zzNotFound").show();
		}
	}});*/
});