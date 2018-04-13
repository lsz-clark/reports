var rootPath = "/xiaoyu";
var __lang = "zh-cn";

$("head").append('<link rel="icon" type="image/x-icon" href="'+rootPath+'/theme/images/favicon.ico">');
// 手机、平板等媒介浏览访问时显示相应比例
$("head").append('<meta name="viewport" content="width=device-width, initial-scale=1">');
// 关键词
$("head").append('<meta name="keywords" content="accounting,mystyle">');

// 公共样式
//$("head").append('<link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">');
$("head").append('<link rel="stylesheet" href="'+rootPath+'/theme/3party/css/bootstrap-3.3.7.min.css">');
$("head").append('<link rel="stylesheet" href="'+rootPath+'/theme/css/common.css">');
$("head").append('<link rel="stylesheet" href="'+rootPath+'/theme/css/mystyle_checkout.css">');

// 公共js
$("head").append('<script type="text/javascript" src="'+rootPath+'/theme/3party/js/jquery.i18n.properties-min-1.0.9.js"></script>');
$("head").append('<script type="text/javascript" src="'+rootPath+'/theme/3party/js/bootstrap-3.3.7.min.js"></script>');
$("head").append('<script type="text/javascript" src="'+rootPath+'/theme/3party/js/jquery.goup.min.js"></script>');
//$("head").append('<script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>	');
$("head").append('<script type="text/javascript" src="'+rootPath+'/theme/js/mystyle_checkout.js"></script>');
$("head").append('<script type="text/javascript" src="'+rootPath+'/theme/js/constant.js"></script>');

/**
 * 初始化
 */
$(function(){
	// 初始化国际化，此方法尽量放到第一位
	initI18nMsg();
	
	// 设置必填项的红星号，必填项
	$(".red-star").each(function(){
		$(this).prepend('<span style="color:red;">*&nbsp;</span>');
	});
	
	// 设置commonBack按钮的默认事件
	$("#commonBackBtn").click(function(){
		// 返回上一页
		window.history.back();
	});
	
	// 快速清除当前文本框
    $(".clear-data").click(function(){
    	$(this).prev().val("").focus();
    });
    
    // 清空
    $("#clearBtn").bind("click",function(){
    	// 文本框
    	$("input[type='text']").val("");
    	$("input[type='number']").val("");
    	$("select").val(0);
    	
    	// 多行文本框
    	$("textarea").val("");
    });
    
    $.goup({
        trigger: 100,
        bottomOffset: 150,
        locationOffset: 100,
        title: '',
        titleAsText: true
    });
});

/**
 * 初始化国际化语言
 */
function initI18nMsg(){
	
	if(localStorage.getItem("language") && localStorage.getItem("language") != "undefined"){
		__lang = localStorage.getItem("language");
	}
	
	$.i18n.properties({
	    name : 'message', // 资源文件名称
	    path : rootPath+'/theme/i18n/', // 资源文件路径
	    mode : 'map', // 用Map的方式使用资源文件中的值
	    language : __lang,//
	    callback : function() {// 加载成功后设置显示内容
	    	// 给带i18n-text类样式的相关元素设置国际化
	        $('.i18n-text').each(function() {
	        	if($(this).attr("placeholder")){
	        		$(this).attr("placeholder", $.i18n.prop($(this).attr("placeholder")));
	        	}else{
	        		$(this).text($.i18n.prop($(this).text()));
	        	}
	        	//if(i18nMsg.indexOf($(this).text()) < 0){
	        	//}
	        });
	    }
	});
}

/**
 * 跳转到主页
 */
function goHome(){
	window.location.href = rootPath + "/portal/home.html";
}

/**
 * ajax-显示页面加载屏遮层，部分
 */
function showRequestLoaddingPart(parentObj, index) {
	if(!index){
		index = "";
	}
	parentObj.append('<div id="dv_bg_loading'+ index +'" class="part_bg_loading"><div class="part_bg_loading_img"></div></div>');
}

/**
 * ajax-关闭页面加载屏遮层，部分
 */
function hideRequestLoaddingPart(index) {
	if(!index){
		index = "";
	}
	$("#dv_bg_loading"+ index).remove();
}

/**
 * ajax-显示页面加载屏遮层，全屏
 */
function showRequestLoadding() {
    $("body").append('<div id="dv_bg_loading" class="dv_bg_loading_css" style="background-color:#000000;opacity:0.3;"><img src="'+rootPath+'/theme/images/ajax-loader.gif"></div>');
}

/**
 * ajax-关闭页面加载屏遮层，全屏
 */
function hideRequestLoadding() {
    $("#dv_bg_loading").remove();
}

/**
 * 解析一般结果，如果出错将会提示
 */
function parseResult(response){
	if(response && response.code != 0){
		// 未登录将提示登录
		if(response.code == "6011000"){
			$.modalTips({
    			context:$.i18n.prop(response.code), 
    			type:"error", 
    			buttons: [{text:$.i18n.prop("confirm"), callfunc:function(){
    			    window.location.href = rootPath+"/portal/common/login.html";
    			}}]
    		});
		}else{
			$.modalTips({context:$.i18n.prop(response.code), type:"error"});
		}
		
		return false;
	}
	return true;
}

/**
 * 查询按钮绑定Enter
 * @param objBtn 查询按钮
 */
function bindEnterKey(objBtn){
	$(document).unbind("keydown").bind("keydown", function(event){
		if(event.keyCode==13){
			objBtn.click(); 
		} 
	});
}

(function($) {
    // 获取浏览器地址栏参数
    $.getUrlParam = function(name){
	   var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
	   var r = window.location.search.substr(1).match(reg);
	   if (r!=null) return unescape(r[2]); return null;
    };
    
    // 提示框
    $.modalTips = function(param){
    	// 如果当前存在显示的提示框，那么不再渲染
    	var modalTips_ = $("div[id^='modal-tips']");
    	if(modalTips_.length > 0 && modalTips_.is(":visible")){
    		return "";
    	}else{
    		modalTips_.remove();
    	}
    	
    	if(!param){
    		param = {};
    	}
    	var modalTipsId = "modal-tips" + Date.parse(new Date());
    	var title = param.title;
    	var context = param.context;
    	var type = param.type;
    	var img_src = "";
    	var buttons = param.buttons;
    	var isShowCloseBtn = param.isShowCloseBtn == false ? false : true;
    	
    	// 默认标题头
    	if(!title){
    		title = $.i18n.prop("notify");
    	}
    	// 默认提示语
    	if(!context){
    		context = $.i18n.prop("operation_success");
    	}
    	// 默认提示图片类型
    	if(!type){
    		type = "success";
    	}
    	switch(type){
    		case "success":
    			img_src = rootPath + "/theme/images/success.png";
    			break;
    		case "error":
    			img_src = rootPath + "/theme/images/error.png";
    			break;
    		case "info":
    			img_src = rootPath + "/theme/images/info.png";
    			break;
    		case "confirm":
    			img_src = rootPath + "/theme/images/confirm.png";
    			break;
    		case "warn":
    		default:
    			img_src = rootPath + "/theme/images/warn.png";
    		    break;
    	}
    	
    	var modalTipsHtml = [];
    	// 头部，生成标题头、关闭按钮
    	modalTipsHtml.push('<div class="modal fade" style="display:block;" id="'+ modalTipsId +'" tabindex="-1" role="dialog">');
    	modalTipsHtml.push('<div class="modal-dialog modal-sm" style="" role="document">');
    	modalTipsHtml.push('<div class="modal-content">');
    	modalTipsHtml.push('<div class="modal-header">');
    	modalTipsHtml.push('<label class="modal-title">'+ title +'</label>');
    	modalTipsHtml.push('<button type="button" class="close" data-dismiss="modal">');
    	modalTipsHtml.push('<span class="glyphicon glyphicon-remove"></span>');
    	modalTipsHtml.push('</button>');
    	modalTipsHtml.push('</div>');
    	
    	// 中部，生成图片、文字
    	modalTipsHtml.push('<div class="modal-body">');
    	modalTipsHtml.push('<img src="'+ img_src +'">&nbsp;'+ context);
    	modalTipsHtml.push('</div>');
    	
    	// 底部，生成按钮
    	modalTipsHtml.push('<div class="modal-footer">');
    	if(isShowCloseBtn){
    		modalTipsHtml.push('<button type="button" class="btn btn-default" data-dismiss="modal">'+$.i18n.prop("close")+'</button>');
    	}
    	
    	if(buttons){
    		$.each(buttons, function(k,v){
        		var $btn = $('<button class="btn btn-danger my-auto-bind" type="button">'+ buttons[k].text +'</button>');
        		modalTipsHtml.push($btn.prop("outerHTML"));
        	});
    	}
    	modalTipsHtml.push('</div>');
    	
    	// 标签结束
    	modalTipsHtml.push('</div></div></div>');
    	
    	// 添加到body元素中去
    	$("body").prepend(modalTipsHtml.join(" "));
    	
    	var modalTipsJQ = $("#"+modalTipsId);
    	
    	modalTipsJQ.modal("show");
    	
    	// 显示，并给按钮绑定事件
    	modalTipsJQ.find("button.my-auto-bind").each(function(i){
    		$(this).bind("click", function(){
    			if(buttons[i].isClose != false){
    				modalTipsJQ.modal("hide");
    			}
    			buttons[i].callfunc();
    		});
    	});
    	
    	return modalTipsJQ;
    };
    
    // 统一ajax请求
    $.myajax = function(param){
    	if(!param || !param.url){
    		// URL 必须传入
    		return;
    	}
    	
    	// 默认是异步请求true
    	if(!param.async && param.async != false){
    		param.async = true;
    	}
    	// HTTP请求类型
    	if(!param.type){
    		param.type = "POST";
    	}
    	// 参数
    	if(!param.data){
    		param.data = {};
    	}
    	// 参数类型
    	if(!param.dataType){
    		param.dataType = "json";
    	}
    	// 默认显示遮罩层
    	if(!param.isLoading && param.isLoading != false){
    		param.isLoading = true;
    	}
    	
    	$.ajax({
            type : param.type,
            async : param.async, 
            url :  rootPath + param.url,
            data : JSON.stringify(param.data),
            dataType : param.dataType,
            contentType : 'application/json;charset=utf-8',  
            beforeSend : function() {if(param.isLoading){showRequestLoadding();}},
            complete : function(){if(param.isLoading){hideRequestLoadding();}},
            success : function(response) {
            	// 如果未登录，那么提示登录
            	if(response.code == "6011000"){
            		// 清除浏览器会话缓存
            		sessionStorage.clear();
    				// 重新渲染
    				renderLogin();
            		// 弹框提示
    				/*$.modalTips({
            			context:$.i18n.prop(response.code), 
            			type:"error", 
            			buttons: [{text:$.i18n.prop("confirm"), callfunc:function(){
            			    window.location.href = rootPath+"/portal/common/login.html";
            			}}]
            		});*/
            	}
            	if(param.callback && typeof param.callback == "function"){
            		// 回调
            		param.callback(response);
                }
            }
        });
    };
    
})(jQuery);

/**
 * 对话框-显示页面加载屏遮层
 */
function showDialogLoadding() {
    $("body").append('<div id="dv_bg_loading" class="dv_bg_loading_css" style="background-color:#000000;opacity:0.4;"></div>');
}

/**
 * 对话框-关闭页面加载屏遮层
 */
function hideDialogLoadding() {
    $("#dv_bg_loading").remove();
}

/**
 * 表格生成器
 * 1、type 当前终端类型，mobile:手机 pc：电脑
 * 2、id 表格id
 * 3、data 请求参数
 * 4、url 请求地址
 * */
function MyTableUtil(){
	// 当前表格信息
	var tableInfo = {};
	
	this.init = function(param){
		tableInfo = param;
		// 表格分页类型
		if(!tableInfo.type){
			tableInfo.type = "pc";
		}
		// 请求参数
		tableInfo.data = {};
		
		// 默认为表格的loading...
    	if(!tableInfo.isLoading){
    		tableInfo.isLoading = false;
    	}
    	
    	// 是否本地分页，默认不是本地分页
    	if(!tableInfo.isLocalPage){
    		tableInfo.isLocalPage = false;
    	}
    	
    	// 渲染表格
    	tableInfo.renderTable = function(response){
    		var tableJQ = $("#"+ tableInfo.id);
    		// 删除临时的tr 或  隐藏表格的loading...
			tableJQ.find("tbody").find('tr[name="tempTR"]').remove();
			// 解析返回结果
			if(parseResult(response) && response.total > 0){
				var trs = [];
				$.each(response.result, function(i){
					var tds = [];
					// 本行特殊设置，先支持style，id
					var specialStyle = null;
					// 回调自定义表格内容函数
					if(tableInfo.callback && typeof tableInfo.callback == "function"){
						specialStyle = tableInfo.callback(response.result[i]);
					}
					// 生成表格内容
					tableJQ.find("thead th[mkey]").each(function(){
						var text = response.result[i][$(this).attr("mkey")];
						if(!text && isNaN(text)){
							text = "";
						}
						
						tds.push('<td>'+ text +'</td>');
					});
					if(!specialStyle){
						trs.push('<tr name="dataTR">'+ tds.join(" ") +'</tr>');
					}else{
						var specialAttr = "";
						
						if(specialStyle.mid){
							// 业务id
							specialAttr += 'mid="'+ specialStyle.mid +'" ';
						}
						if(specialStyle.mstyle){
							// 本行行内样式
							specialAttr += 'style="'+ specialStyle.mstyle +'" ';
						}
						if(specialStyle.mclass){
							// 本行类样式
							specialAttr += 'class="'+ specialStyle.mclass +'" ';
						}
						
						trs.push('<tr name="dataTR" '+ specialAttr +'>'+ tds.join(" ") +'</tr>');
					}
					
					tds.splice(0, tds.length);
				});
				
				tableJQ.find("tbody").append(trs.join(" "));
				
				// tr绑定单击事件，默认选中第一个td中的复选框或是单选框
				tableJQ.find("tbody tr").bind("click", function(){
					$(this).find("td:first").find("input[type='radio']").attr("checked","checked");
					$(this).find("td:first").find("input[type='checkbox']").attr("checked","checked");
				});
				
				tableInfo.total = response.total;
			}else{
				if(tableJQ.find('tr[name="dataTR"]').length <= 0){
					var th_td_num = tableJQ.find("thead").find("th").length;
					// 没数据啦
					tableJQ.find("tbody").append('<tr name="tempTR"><td colspan="'+ th_td_num +'" style="color:gray;" align="center">'+ $.i18n.prop("no_record_be_found") +'</td></tr>');
				}
				tableInfo.total = 0;
			}
			
			// 绑定事件
			if(tableInfo.maction){
				tableJQ.find("tbody tr td").find("a[maction]").unbind("click").bind("click", function(){
					var mid = $(this).parent().parent().attr("mid");
					tableInfo.maction[$(this).attr("maction")](mid, $(this));
				});
			}
			
			// ========================================== 设置分页
			// 之前的分页删除
			$("#"+ tableInfo.id).next(".mypage").remove();
			
			if(tableInfo.total > 0){
				// 最大页数
				var maxPage = parseInt(tableInfo.total/tableInfo.data.page.pageSize);
				if(maxPage < (tableInfo.total/tableInfo.data.page.pageSize)){
					maxPage = maxPage + 1;
				}
				
				// 最小页数
				var minPage = 1;
				
				// 是否有上一页
				var hasPrev = tableInfo.data.page.pageIndex == 1 ? false : true;
				
				// 是否有下一页
				var hasNext = false;
				if(maxPage > 1 && tableInfo.data.page.pageIndex < maxPage){
					hasNext = true;
				}
				
				var page = [];
				// 本地分页添加间距
				if(tableInfo.isLocalPage){
					page.push('<nav class="mypage" aria-label="Page navigation" style="float: right;margin-right: 5px;">');
				}else{
					page.push('<nav class="mypage" aria-label="Page navigation" style="float: right;">');
				}
				
				
				page.push('<ul class="pagination" style="margin-top:0px;float: right; ">');
				
				// 上一页
				if(hasPrev){
					// 启用
					page.push('<li><a href="#" class="mypage-prev" aria-label="Previous"><span aria-hidden="true">&lt;</span></a></li>');
				}else{
					// 禁用
					page.push('<li class="disabled"><a href="#" aria-label="Previous"><span aria-hidden="true">&lt;</span></a></li>');
				}
				
				// 页码
				var nums = calcNums(tableInfo.data.page.pageIndex, maxPage, 5);
				for(var i = 0; i < nums.length; i++){
					if(tableInfo.data.page.pageIndex == nums[i]){
						// 选中当前页
						page.push('<li class="active"><a href="#">'+ nums[i] +'</a></li>');
					}else{
						page.push('<li><a href="#" class="mypage-num">'+ nums[i] +'</a></li>');
					}
				}
				
				// 下一页
				if(hasNext){
					// 启用
					page.push('<li><a href="#" class="mypage-next" aria-label="Next"><span aria-hidden="true">&gt;</span></a>');
				}else{
					// 禁用
					page.push('<li class="disabled"><a href="#" aria-label="Next"><span aria-hidden="true">&gt;</span></a>');
				}
				page.push('</ul>');
				
				// 记录数
				page.push('<div class="pagination-total" style="float: right;margin-top: 8px;margin-right: 5px;">'+$.i18n.prop("total")+tableInfo.total+ $.i18n.prop("record") +'</div>');
				
				page.push('<div style="float: right;margin-right: 5px;">');
				page.push('<div class="btn-group">');
				page.push('<button type="button" style="height: 33px;width:50px;" class="btn btn-default dropdown-toggle pagination-size" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'+tableInfo.data.page.pageSize+'&nbsp;<span class="caret"></span></button>');
				page.push('<ul class="dropdown-menu" style="width: 50px;min-width: 0px;">');
				
				// 页大小
				page.push('<li><a href="#" class="mypage-size" style="padding: 3px 12px;">5</a></li>');
				page.push('<li><a href="#" class="mypage-size" style="padding: 3px 12px;">10</a></li>');
				page.push('<li><a href="#" class="mypage-size" style="padding: 3px 12px;">30</a></li>');
				page.push('<li><a href="#" class="mypage-size" style="padding: 3px 12px;">50</a></li>');
				page.push('<li><a href="#" class="mypage-size" style="padding: 3px 12px;">100</a></li>');
				
				page.push('</ul>');
				page.push('</div>');
				page.push('</div>');
				page.push('</nav>');
				
				$("#"+ tableInfo.id).after(page.join(" "));
				
				// 绑定事件
				var $mypage = $("#"+ tableInfo.id).next(".mypage");
				
				// 上一页
				$mypage.find(".mypage-prev").click(function(){
					tableInfo.data.page.pageIndex = tableInfo.data.page.pageIndex - 1;
					$("#"+ tableInfo.id).find("tbody tr").remove();
					loadData(tableInfo.data);
				});
				
				// 下一页
				$mypage.find(".mypage-next").click(function(){
					tableInfo.data.page.pageIndex = tableInfo.data.page.pageIndex + 1;
					$("#"+ tableInfo.id).find("tbody tr").remove();
					loadData(tableInfo.data);
				});
				
				// 页码
				$mypage.find(".mypage-num").click(function(){
					tableInfo.data.page.pageIndex = parseInt($(this).text());
					$("#"+ tableInfo.id).find("tbody tr").remove();
					loadData(tableInfo.data);
				});
				
				// 页大小
				$mypage.find(".mypage-size").click(function(){
					tableInfo.data.page.pageSize = parseInt($(this).text());
					tableInfo.data.page.pageIndex = 1;
					$("#"+ tableInfo.id).find("tbody tr").remove();
					loadData(tableInfo.data);
				});
			}
    	}
	}
	
	this.reload = function(data_){
		this.clearAll();
		loadData(data_);
	}
	
	this.refresh = function(){
		this.clearAll();
		this.load(tableInfo.data);
	}
	
	this.load = function(data_){
		loadData(data_);
	}
	
	var loadData = function(data_){
		// 请求参数
		if(data_){
			tableInfo.data = data_;
		}
		
		var tableJQ = $("#"+ tableInfo.id);
		var th_td_num = tableJQ.find("thead").find("th").length;
		
		if(!tableInfo.isLoading){
			// 显示表格的loading...
			tableJQ.find("tbody").append('<tr name="tempTR"><td colspan="'+ th_td_num +'" align="center"><img src="'+ rootPath +'/theme/images/loading.gif"></td></tr>');
		}
		
		// 设置默认分页
		if(!tableInfo.data.page){
			tableInfo.data.page = {};
			tableInfo.data.page['pageSize'] = 10;// 页大小
			tableInfo.data.page['pageIndex'] = 1;// 当前页
		}
		
		// 本地分页
		if(tableInfo.response){
			var newResponse = {};
			$.extend(true, newResponse, tableInfo.response);// 深度copy
			// 回调用户自定义分页查询方法获取最新数据，格式必须是 ListResponse json类型
			tableInfo.renderTable(tableInfo.localQuery(newResponse, tableInfo));// 渲染table
		}else{
			// 默认分页
			// ajax发送请求
			$.myajax({
				data: tableInfo.data,
				url: tableInfo.url,
				isLoading: tableInfo.isLoading,
				callback: function(response){
					// 本地分页，需要存储第一次查询出来的数据
					if(tableInfo.isLocalPage){
						var newResponse = {};
						$.extend(true, newResponse, response);// 深度copy
						// 回调用户自定义分页查询方法获取最新数据，格式必须是 ListResponse json类型
						tableInfo.renderTable(tableInfo.localQuery(newResponse, tableInfo));// 渲染table
					}else{
						// 渲染table
						tableInfo.renderTable(response);
					}
				}
			});
		}
	}
	
	// 清空表格
	this.clearAll = function(){
		// 移除元素
		$("#"+ tableInfo.id).find("tbody tr").remove();
	}
};

/**
 * 计算页码
 * @param pageIndex 当前页
 * @param maxNum 最大页大小
 * @param showNumSize 显示多少页码
 * @returns 生成页码
 */
function calcNums(pageIndex, maxNum, showNumSize){
	var hlafSize = parseInt(showNumSize/2);
	var minPageIndex = pageIndex;
	var maxPageIndex = pageIndex;
	var nums = [];
	var minCount = 1;
	
	var isPush = false;
	for(var i=0; i<showNumSize; i++){
		if(minPageIndex > 1 && minCount <= hlafSize){
			minPageIndex = minPageIndex -1;
			nums.push(minPageIndex);
			minCount++;
		}else if(!isPush){
			nums.push(pageIndex);
			isPush = true;
		}else if(maxPageIndex < maxNum){
			maxPageIndex = maxPageIndex +1;
			nums.push(maxPageIndex);
		}
	}
	
	return sortarr(nums);
}

/**
 * 冒泡排序
 * @param arr 待排序的数组
 * @returns arr 排序后的数组
 */
function sortarr(arr){
    for(i=0; i<arr.length-1; i++){
        for(j=0; j<arr.length-1-i; j++){
            if(arr[j]>arr[j+1]){
                var temp=arr[j];
                arr[j]=arr[j+1];
                arr[j+1]=temp;
            }
        }
    }
    return arr;
}