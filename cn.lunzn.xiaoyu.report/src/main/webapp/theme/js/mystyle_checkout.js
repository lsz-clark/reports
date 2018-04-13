/*
 * AK 客户端校验框架
 * 
 * Author Clark
 * Time 2013-03-25 18:36:10
 * 
 * 本框架基于JQuery-1.8.3与CSS3,如果你的浏览器不支持CSS3，可能样式不怎么好看
 * 
 * 1.提供公共校验规则以及自定义规则，主要针对文本框、下拉框、多行文本框等进行校验
 * 
 * 2.提供id、class、name三种选择器进行绑定校验
 * 
 * 3.提供FireFox、谷歌浏览器支持
 */

/**
 * 定义命名空间
 */
$.AK = function() {
};

/**
 * 根据value获得其长度，中文算两个字符
 */
String.prototype.len = function() {
	return this.replace(/[^\x00-\xff]/g, "ak").length;
};

/**
 * 判断是否为空
 */
$.AK.isEmpty = function(str) {
	if (undefined === str || null == str || str.length <= 0) {
		return true;
	}
	return false;
};

/**
 * 判断是否不为空
 */
$.AK.isNotEmpty = function(str) {
	return !$.AK.isEmpty(str);
};

/**
 * 移除字符串中的最后一个字符
 * 
 * @param str
 *            需要移除字符串
 * @returns 返回处理好的字符串
 */
$.AK.removeLastChar = function(str) {
	if ($.AK.isNotEmpty(str)) {
		return str.substring(0, str.length - 1);
	}
	return str;
};

/**
 * 定义全局的公共校验规则
 */
var ak_roles = new Array();

/*
 * 默认的提示信息
 */
var ak_default_msg = "数据无效.";
var ak_default_empty_msg = "不能为空.";
var ak_default_port_msg = "无效的端口号.";
var ak_default_num_msg = "无效的数字";
var ak_default_ip_msg = "无效的主机地址.";
var ak_default_url_msg = "无效的URL地址.";
var ak_default_email_msg = "无效的邮箱地址.";

/**
 * 初始化公共校验规则
 */
ak_publicRoles();

/**
 * 校验绑定扩展方法
 */
jQuery.fn.extend({
	checkout : function(options) {
		options = options || {};

		// 校验对象，包括被校验的元素信息
		var source = options.source == undefined ? null : options.source;

		if (null == source) {
			return;
		}

		// 触发校验事件类型
		this.type = options.type == undefined ? "click" : options.type;

		// 验证成功需要调用的函数
		var completeFunction = options.completeFunction == undefined ? null
				: options.completeFunction;

		this.onReady = options.onReady == undefined ? false : options.onReady;

		// 绑定校验，进入页面就开始校验
		if(this.onReady)
		{
			source.validate();
		}

		$(this).bind(this.type, function(e) {

			$(window).unbind("resize");

			// 当窗口改变大小时，重新定位图片和提示
			$(window).resize(function() {
				source.validate();
			});

			if (source.validate()) {
				if (completeFunction != null) {
					// 执行业务方法
					completeFunction();
				} else {
					document.forms[0].submit();
				}
			}
		});
	}
});

/**
 * 校验器类
 */
function AKCheckout() {
	// 存储需要校验的元素
	var checkouts = new Array();

	// 添加需要校验的元素
	this.addField = function(options) {
		checkouts.push(new this.Field(options));
		this.validate("onstep");
	};
    
	// 移除指定元素
	this.removeField = function(key){
		for ( var i = 0; i < checkouts.length; i = i + 1) {
			if(checkouts[i].id == key || checkouts[i].name == key || checkouts[i].className == key){
				checkouts[i].getJQueryElement.each(function() {
					// 移除元素原先的校验事件
					$(this).unbind("keyup");
					
					$.AK.addInformation($(this), true, "", false);
				});
				checkouts.splice(i, 1);
			}
		}
	};
	
	// 给元素添加校验方法
	this.validate = function(stepFlag) {
		var faildCount = 0;

		// 循环所有需要添加校验的元素，并给所有添加onkeyup或是onchange事件
		for ( var i = 0; i < checkouts.length; i = i + 1) {
			// id：绑定为单绑定，以name或是class绑定的为多绑定
			checkouts[i].getJQueryElement.each(function() {
				// 移除元素原先的校验事件
				$(this).unbind("keyup");
				if (!$.AK.bindCheckout($(this), checkouts[i], stepFlag)) {
					faildCount = faildCount + 1;
				}
			});
		}
		return faildCount <= 0;
	};

	// 校验器模型类
	this.Field = function(options) {
		options = options || {};

		// 以ID的方式绑定
		this.id = options.id == undefined ? null : options.id;

		// 以名称的方式绑定，多绑定
		this.name = options.name == undefined ? null : options.name;

		// 以类样式的方式绑定，多绑定
		this.className = options.className == undefined ? null
				: options.className;

		// 校验失败提示元素的id(如果没有传入则默认为id，name，className为宿主)
		this.showElement = options.showElement == undefined ? null
				: options.showElement;

		// 依赖的元素
		this.dependId = options.dependId == undefined ? null : options.dependId;

		// 依赖得值
		this.dependVal = options.dependId == undefined ? null
				: options.dependVal;

		// 元素值是否必须输入
		this.isRequired = options.isRequired == undefined ? false
				: options.isRequired;

		// 校验规则
		this.validType = options.validType == undefined ? null
				: options.validType;

		// 自定义正则表达式
		this.expr = options.expr == undefined ? null : options.expr;

		// 文本框为空时的提示信息
		this.emptyMsg = options.emptyMsg == undefined ? ak_default_empty_msg
				: options.emptyMsg;

		// 文本框的默认值
		this.defaultValue = options.defaultValue == undefined ? null
				: options.defaultValue;

		// 通过id、name、class选择器来获得JQuery对象
		this.getJQueryElement = $.AK.getFieldElement({
			id : this.id,
			name : this.name,
			className : this.className
		});

		// 获得本元素的规则
		this.getRole = $.AK.getRole(this.validType);

		// 获得本元素的参数
		this.getParams = $.AK.getParams(this.validType);

		// 元素通过校验失败时的提示信息
		this.invalidMsg = options.invalidMsg == undefined ? $.AK
				.setDefaultMessage(this.getRole) : $.AK.setMessage(
				options.invalidMsg, this.getParams);

		// 校验正则表达式是否满足
		this.checkExpr = function(val) {
			if (null != this.expr && !this.expr.test(val)) {
				return false;
			}
			return true;
		};
	};
};

/**
 * 校验规则类
 */
function AKRole(key_, roleFunc_, msg_) {
	// 规则名
	this.key = key_;
	this.roleFunc = roleFunc_;
	this.msg = msg_;
}

/**
 * 绑定校验方法，开始对元素的值进行校验
 */
$.AK.bindCheckout = function(element, checkout, stepFlag) {
	// 获得这个元素的值
	var val = element.val();
	// 如果该元素为隐藏元素，则判断是否有错误宿主,如果没有这时不会校验该元素，直接返回true
	if (element.is(":hidden") && null == checkout.showElement) {
		return true;
	}

	// 如果是多选框，则判断是否有option就行
	if ($.AK.isNotEmpty(element.attr("multiple"))) {
		var count = element.children("option").size();
		val = count <= 0 ? null : count;
	}

	// 给元素（如文本框）绑定校验
	element.bind("keyup change blur", function() {
		val = $(this).val();
		// 如果是多选框，则判断是否有option就行
		if ($.AK.isNotEmpty($(this).attr("multiple"))) {
			count = $(this).children("option").size();
			val = count <= 0 ? null : count;
		}
		$.AK.initCheckout(element, checkout, val, false);
	});

	if(stepFlag != "onstep"){
		// 提交元素（一般指触发按钮）绑定校验
		return $.AK.initCheckout(element, checkout, val, true);
	}else{
		return true;
	}
};

/**
 * 初始校验，对元素的值进行校验，此函数只校验默认值与空值
 */
$.AK.initCheckout = function(element, checkout, val, hideFlag) {
	// 如果有依赖元素那么校验是否满足依赖,不满足则不进行下一步校验
	if (null != checkout.dependId
			&& $("#" + checkout.dependId).val() != checkout.dependVal) {
		return true;
	}

	// 如果有错误宿主则换成它显示错误信息
	if (null != checkout.showElement) {
		element = $("#" + checkout.showElement);
	}

	// 默认值校验
	if ($.AK.isNotEmpty(checkout.defaultValue) && $.AK.isNotEmpty(val)
			&& val == checkout.defaultValue) {
		$.AK.addInformation(element, true, "", hideFlag);
		return true;
	}

	// 空值校验
	if (!checkout.isRequired) {
		if ($.AK.isEmpty(val)) {
			$.AK.addInformation(element, true, "", hideFlag);
			return true;
		}
		// 可以为空的元素，而又有值时，需要对其进行校验(是否符合规则或是正则表达式)
		return $.AK.invalidateFunc(element, checkout, val, hideFlag);
	} else {
		// 必须输入的元素，为空值时添加错误信息提示,如果为下拉框则判断是否为-1
		if ($.AK.isEmpty(val)) {
			$.AK.addInformation(element, false, checkout.emptyMsg, hideFlag);
			return false;
		}

		// 校验元素的值是否合法(是否符合规则或是正则表达式)
		return $.AK.invalidateFunc(element, checkout, val, hideFlag);
	}
};

/**
 * 校验规则与自定义正则校验
 */
$.AK.invalidateFunc = function(element, checkout, val, hideFlag) {
	// 没有校验类型
	if (null == checkout.getRole) {
		// 是否带有正则表达式
		if (!checkout.checkExpr(val)) {
			$.AK.addInformation(element, false, checkout.invalidMsg, hideFlag);
			return false;
		}
		$.AK.addInformation(element, true, "", hideFlag);
		return true;
	} else {
		// 是否带有正则表达式
		if (!checkout.checkExpr(val)) {
			$.AK.addInformation(element, false, checkout.invalidMsg, hideFlag);
			return false;
		}

		// 规则校验
		if (checkout.getRole.roleFunc(val, checkout.getParams)) {
			$.AK.addInformation(element, true, "", hideFlag);
			return true;
		} else {
			$.AK.addInformation(element, false, checkout.invalidMsg, hideFlag);
			return false;
		}
	}

};

/**
 * 根据校验结果对元素添加或移除提示信息
 * @param element 这个为输入框元素（注意它是JQuery对象哦），也就是给谁身上加上错误提示框的地方
 * @param isCorrect 校验失败时成功，其值只有为false时，才需要加上错误提示框
 * @param msg 错误提示信息
 * @param hideFlag 错误提示框是否隐藏
 */
$.AK.addInformation = function(element, isCorrect, msg, hideFlag) {
	// 元素的父节点
	var element_parent = element.parent();
	// 算出这个元素的X,Y
	var x_ = element.position().left;
	var y_ = element.position().top;
	// 算出这个元素的宽度
	var inner_width = element.innerWidth();
	element.removeAttr("style");

	element_parent.children(".ak_checkout_talk_default").remove();

	if (isCorrect) {

	} else {
		// 设置文本框边框颜色--红色
		element.css("border", "1px solid #DF6027");

		// 给输入框加上错误对话框
		element_parent.append("<div class='ak_checkout_talk_default'>" + msg
				+ "</div>");

		// 错误提示信息字数过多可能存在多行问题，这时需要判断字符数超过多少个，然后算出上间距是多少
		var y_top = 35;
		if (msg.len() >= 42) {
			y_top = 55;
		} else if (msg.len() >= 84) {
			y_top = 75;
		}

		// 给错误对话框加上 左间距x与上间距y
		element_parent.find(".ak_checkout_talk_default").css({
			"left" : (x_ + inner_width / 7),
			"top" : (y_ - y_top)
		});

		if (hideFlag) {
			element_parent.children(".ak_checkout_talk_default").hide();
		}

		// 鼠标移到输入框上面需要显示错误对话框
		element.mouseover(function() {
			element_parent.children(".ak_checkout_talk_default").fadeIn(100);
		}).mouseout(function() {
			element_parent.children(".ak_checkout_talk_default").fadeOut();
		});
	}
};

/**
 * 默认提示信息
 */
$.AK.setDefaultMessage = function(role) {
	if (null != role && $.AK.isNotEmpty(role.msg)) {
		return role.msg;
	}
	return ak_default_msg;
};

/**
 * 设置消息
 */
$.AK.setMessage = function(message_, params) {
	var newMsg = message_;

	if ($.AK.isEmpty(params)) {
		return newMsg;
	} else {
		// 覆盖、填充消息，如length({0}-{1})-->length(10-20)
		for ( var i = 0; i < params.length; i++) {
			newMsg = newMsg.replace("{" + i + "}", params[i]);
		}
		return newMsg;
	}
};

/**
 * 获取参数
 */
$.AK.getParams = function(key) {
	if ($.AK.isEmpty(key)) {
		return null;
	}

	var params = null;
	if (key.indexOf("[") > 0) {
		params = key.substring(key.indexOf("["), key.lastIndexOf("]") + 1);
		// 把字符串转换成数组
		params = eval("(" + params + ")");
	}
	return params;
};

/**
 * 通过用户传递的校验规则来获取规则对象
 */
$.AK.getRole = function(key) {
	if ($.AK.isEmpty(key)) {
		return null;
	}

	var true_key = "";

	if (key.indexOf("[") == -1) {
		true_key = key;
	} else {
		true_key = key.substring(0, key.indexOf("["));
	}

	for ( var i = 0; i < ak_roles.length; i = i + 1) {

		if (ak_roles[i].key == true_key) {

			return ak_roles[i];
		}
	}
	return null;
};

/**
 * 通过id或者name或者class选择器来选择元素（JQuery对象）
 */
$.AK.getFieldElement = function(options) {
	options = options || {};
	var field = null;
	if (options.id != null) {
		// 根据ID查找元素
		field = $("#" + options.id);
	} else if (options.name != null) {
		// 根据NAME查找元素
		field = $("*[name='" + options.name + "']");

	} else {
		// 根据类样式查找元素
		field = $("." + options.className);
	}
	return field;
};

/**
 * 把字符改成数字（十进制）
 */
$.AK.myParseInt = function(number) {
	return parseInt(number, 10);
};

/**
 * 检查是否满足范围
 */
function numberRange(num, params) {
	if (null == params || typeof params[0] == "undefined")
		return true;
	// 小于最小长度 为false
	if (num < $.AK.myParseInt(params[0]))
		return false;
	// 大于最大长度 为false
	if (num > $.AK.myParseInt(params[1]))
		return false;
	return true;
};

/**
 * 检查是否有参数
 */
function ak_checkParams(params) {
	if ($.AK.isEmpty(params)) {
		return false;
	}
	return true;
};

/**
 * 检查参数是否为id
 */
function ak_checkId(param) {
	if (!isNaN(param)) {
		return false;
	}

	if (param[0].indexOf("#") >= 0 && param[0].indexOf("#") == 0) {
		return true;
	}
	return false;
};

/**
 * 初始化公共规则
 */
function ak_publicRoles() {
	// 规则1：相等 EQ
	ak_roles.push(new AKRole("AK_EQ", function(value, params) {
		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if (value == $(params[0]).val())
					return true;
			} else {
				// 直接与参数比较
				for ( var i = 0; i < params.length; i = i + 1) {
					if (value == params[i])
						return true;
				}
			}
		}
		return false;
	}, ak_default_msg));

	// 规则2：不等NE
	ak_roles.push(new AKRole("AK_NE", function(value, params) {
		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if (value != $(params[0]).val())
					return true;
			} else {
				var isCorrect = true;
				// 直接与参数比较
				for ( var i = 0; i < params.length; i = i + 1) {
					if (value == params[i]) {
						isCorrect = false;
						break;
					}
				}
				return isCorrect;
			}
		}
		return false;
	}, ak_default_msg));

	// 规则3：小于 LT
	ak_roles.push(new AKRole("AK_LT", function(value, params) {

		// 当前值必须是数字，否则返回false
		if (isNaN(value)) {
			return false;
		}

		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if ($.AK.myParseInt(value) < $.AK
						.myParseInt($(params[0]).val())) {
					return true;
				}
			} else {
				// 直接与参数比较
				if ($.AK.myParseInt(value) < $.AK.myParseInt(params[0])) {
					return true;
				}
			}
		}
		return false;
	}, ak_default_msg));

	// 规则4：大于 GT
	ak_roles.push(new AKRole("AK_GT", function(value, params) {

		// 当前值必须是数字，否则返回false
		if (isNaN(value)) {
			return false;
		}

		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if ($.AK.myParseInt(value) > $.AK
						.myParseInt($(params[0]).val())) {
					return true;
				}
			} else {
				// 直接与参数比较
				if ($.AK.myParseInt(value) > $.AK.myParseInt(params[0])) {
					return true;
				}
			}
		}
		return false;
	}, ak_default_msg));

	// 规则5：小于等于 LE
	ak_roles.push(new AKRole("AK_LE", function(value, params) {
		// 当前值必须是数字，否则返回false
		if (isNaN(value)) {
			return false;
		}

		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if ($.AK.myParseInt(value) <= $.AK.myParseInt($(params[0])
						.val())) {
					return true;
				}
			} else {
				// 直接与参数比较
				if ($.AK.myParseInt(value) <= $.AK.myParseInt(params[0])) {
					return true;
				}
			}
		}
		return false;
	}, ak_default_msg));

	// 规则6：大于等于 GE
	ak_roles.push(new AKRole("AK_GE", function(value, params) {
		// 当前值必须是数字，否则返回false
		if (isNaN(value)) {
			return false;
		}

		if (ak_checkParams(params)) {
			if (ak_checkId(params[0])) {
				// 与对应的ID元素的值进行比较
				if ($.AK.myParseInt(value) >= $.AK.myParseInt($(params[0])
						.val())) {
					return true;
				}
			} else {
				// 直接与参数比较
				if ($.AK.myParseInt(value) >= $.AK.myParseInt(params[0])) {
					return true;
				}
			}
		}
		return false;
	}, ak_default_msg));

	// 规则7：大于min 小于max
	ak_roles.push(new AKRole("AK_LG",
			function(value, params) {
				// 当前值必须是数字，否则返回false
				if (isNaN(value)) {
					return false;
				}

				if (ak_checkParams(params) && params.length > 1) {
					if (ak_checkId(params[0]) && ak_checkId(params[1])) {
						// 与对应的ID元素的值进行比较
						if ($.AK.myParseInt(value) > $.AK.myParseInt($(
								params[0]).val())
								&& $.AK.myParseInt(value) < $.AK.myParseInt($(
										params[1]).val())) {
							return true;
						}
					} else {
						// 直接与参数比较
						if ($.AK.myParseInt(value) > $.AK.myParseInt(params[0])
								&& $.AK.myParseInt(value) < $.AK
										.myParseInt(params[1])) {
							return true;
						}
					}
				}
				return false;
			}, ak_default_msg));

	// 规则8：大于等于min 小于等于max
	ak_roles.push(new AKRole("AK_LEGE",
			function(value, params) {
				// 当前值必须是数字，否则返回false
				if (isNaN(value)) {
					return false;
				}

				if (ak_checkParams(params) && params.length > 1) {
					if (ak_checkId(params[0]) && ak_checkId(params[1])) {
						// 与对应的ID元素的值进行比较
						if ($.AK.myParseInt(value) >= $.AK.myParseInt($(
								params[0]).val())
								&& $.AK.myParseInt(value) <= $.AK.myParseInt($(
										params[1]).val())) {
							return true;
						}
					} else {
						// 直接与参数比较
						if ($.AK.myParseInt(value) >= $.AK
								.myParseInt(params[0])
								&& $.AK.myParseInt(value) <= $.AK
										.myParseInt(params[1])) {
							return true;
						}
					}
				}
				return false;
			}, ak_default_msg));

	// 规则9：端口号
	ak_roles.push(new AKRole("AK_PORT", function(value, params) {
		var re = /^\d{4,6}$/;
		return re.test(value);
	}, ak_default_port_msg));

	// 规则10：数字
	ak_roles.push(new AKRole("AK_NUM", function(value, params) {
		var re = /^\d+$/;
		if (!re.test(value)) {
			return false;
		}

		if (!numberRange($.AK.myParseInt(value), params)) {
			return false;
		}

		return true;
	}, ak_default_num_msg));

	// 规则11：IP
	ak_roles
			.push(new AKRole(
					"AK_IP",
					function(value, params) {
						var re = /^(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5]).(d{1,2}|1dd|2[0-4]d|25[0-5])$/;
						return re.test(value);
					}, ak_default_ip_msg));

	// 规则12：URL
	ak_roles
			.push(new AKRole(
					"AK_URL",
					function(value, params) {
						if (!numberRange(value.length, params)) {
							return false;
						}
						var expression = /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i;
						return expression.test(value);
					}, ak_default_url_msg));

	// 规则13：Email
	ak_roles.push(new AKRole("AK_EMAIL", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var expression = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
		return expression.test(value);
	}, ak_default_email_msg));

	// 规则14：手机号码
	ak_roles
			.push(new AKRole(
					"AK_MN",
					function(value, params) {
						var expression = /^13[0-9]{1}[0-9]{8}$|^15[012356789]{1}[0-9]{8}$|^18[0256789]{1}[0-9]{8}$/;
						return value.length == 11 && expression.test(value);
					}, ak_default_num_msg));

	// 规则15：长度
	ak_roles.push(new AKRole("AK_LENGHT", function(value, params) {
		return numberRange(value.length, params);
	}, ak_default_msg));

	// 规则16：校验字符A,由数字、字母、下划线、横线、小数点组成
	ak_roles.push(new AKRole("AK_LA", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var re = /^([0-9a-zA-Z_.\-])+$/;
		return re.test(value);
	}, ak_default_msg));

	// 规则17：校验字符B,以字母开头的任意字符,除空格以外的字符
	ak_roles.push(new AKRole("AK_LB", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var re = /^([a-zA-Z]{1})+\S+$/;
		return re.test(value);
	}, ak_default_msg));

	// 规则18：校验图片
	ak_roles
			.push(new AKRole(
					"AK_PIC",
					function(value, params) {
						var re = /^.*?\.([j,J][p,P][g,G]|[j,J][p,P][e,E][g,G]|[b,B][m,M][p,P]|[g,G][i,I][f,F]|[p,P][n,N][g,G])$/;
						return re.test(value);
					}, ak_default_msg));

	// 规则19：校验字符C,由数字、字母、下划线、横线、小数点、空格组成
	ak_roles.push(new AKRole("AK_LC", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var re = /^([0-9a-zA-Z_.\-\s])+$/;
		return re.test(value);
	}, ak_default_msg));

	// 规则20：校验字符D,由数字、字母、下划线、中文组成
	ak_roles.push(new AKRole("AK_LD", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var re = /^[0-9a-zA-Z_\u3E00-\u9FA5]+$/;
		return re.test(value);
	}, ak_default_msg));

	// 规则21：校验必须包含2个以上的字母、数字、特殊符号，所包含的字母、数字、特殊符号顺序不限
	ak_roles.push(new AKRole("AK_PW", function(value, params) {
		if (!numberRange(value.length, params)) {
			return false;
		}
		var re = /(?=.*\d.*\d+)(?=.*[a-zA-Z].*[a-zA-Z]+)/;
		return re.test(value);
	}, ak_default_msg));

	// 规则22：校验身份证号码18位前17位为数字，最后一位是校验位，可能为数字或字符X
	ak_roles.push(new AKRole("AK_CARD", function(value, params) {
		var re = /(^\d{17}([0-9]|X)$)/;
		return re.test(value);
	}, ak_default_msg));

	// 规则23：校验电话号码正则表达式（支持手机号码，3-4位区号，7-8位直播号码，1－4位分机号）
	ak_roles
			.push(new AKRole(
					"AK_TELLPHONE",
					function(value, params) {
						var re = /((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/;
						return re.test(value);
					}, ak_default_msg));
};