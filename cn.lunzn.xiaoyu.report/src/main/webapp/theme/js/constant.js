if (typeof (Constant) == 'undefined') {
	Constant = {};
}
(function() {
	var mConstants = {
	       accountType : {
		       income : 1,
		       cost : 0
		   },
		   accountTypeMap : {
		       1 : "income",// 国际化key
		       0 : "cost",// 国际化key
		   },
		   
		   handleFlag : {
			   handle_no : 1,
			   handle_ok : 2
		   },
		   handleFlagMap : {
		       1 : "handle_no",// 国际化key
		       2 : "handle_ok",// 国际化key
		   },
		   
		   auditType : {
			   sz : 1,
			   zz : 2
		   },
		   auditTypeMap : {
		       1 : "sz",// 国际化key
		       2 : "zz",// 国际化key
		   },
		   stateType : {
			   edit : 1,
			   audit : 2,
			   pass : 3,
			   reject : 4
		   },
		   stateMap : {
		       1 : "option_edit",// 国际化key 
		       2 : "option_audit",// 国际化key
		       3 : "option_pass",// 国际化key
		       4 : "option_reject",// 国际化key
		   },
	};

	$.extend(Constant, mConstants, true);
})();