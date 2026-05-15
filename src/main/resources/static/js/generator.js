$(function () {
    $("#jqGrid").jqGrid({
        url: 'sys/generator/list',
        datatype: "json",
        colModel: [
			{ label: '表名', name: 'tableName', width: 100, key: true },
			{ label: 'Engine', name: 'engine', width: 70},
			{ label: '表备注', name: 'tableComment', width: 100 },
			{ label: '创建时间', name: 'createTime', width: 100 }
        ],
		viewrecords: true,
        height: 385,
        rowNum: 10,
		rowList : [10,30,50,100,200],
        rownumbers: true,
        rownumWidth: 25,
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page",
            rows:"limit",
            order: "order"
        },
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
        }
    });
});

var vm = new Vue({
	el:'#app',
	data:{
		q:{
			tableName: null,
            generateType: 'boot',
            helioFrameworkVersion: 'v2',
            queryFormSchema: true,
            serviceAndImpl: false,
            mybatisXML: false,
            useYesOrNoEnum: true,
            useEnabledStatusEnum: true,
            mainPath: '',
            package: '',
            moduleName: '',
            tablePrefix: ''
		}
	},
	created: function() {
		this.loadSettings();
	},
	methods: {
		query: function () {
				$("#jqGrid").jqGrid('setGridParam',{
	                postData:{'tableName': vm.q.tableName},
	                page:1
	            }).trigger("reloadGrid");
			},
			generator: function() {
	            const tableNames = getSelectedRows();
	            if(tableNames == null){
	                return ;
	            }
	            location.href = "sys/generator/code?tables=" + tableNames.join()
	                + '&generateType=' + vm.q.generateType
	                + '&helioFrameworkVersion=' + vm.q.helioFrameworkVersion
	                // 借助 !! 操作符转换为 boolean 型
	                + '&queryFormSchema=' + !!vm.q.queryFormSchema
	                + '&serviceAndImpl=' + !!vm.q.serviceAndImpl
	                + '&mybatisXML=' + !!vm.q.mybatisXML
	                + '&useYesOrNoEnum=' + !!vm.q.useYesOrNoEnum
	                + '&useEnabledStatusEnum=' + !!vm.q.useEnabledStatusEnum
	            ;
			},
			loadSettings: function() {
				$.ajax({
					url: 'sys/generator/settings',
					type: 'GET',
					dataType: 'json',
					contentType: 'application/json',
					success: function(r) {
						if (r.code === 0 && r.settings) {
							vm.q.mainPath = r.settings.mainPath || '';
							vm.q.package = r.settings.package || '';
							vm.q.moduleName = r.settings.moduleName || '';
							vm.q.tablePrefix = r.settings.tablePrefix || '';
						}
					}
				});
			},
			saveSettings: function() {
				var settings = {
					mainPath: vm.q.mainPath,
					package: vm.q.package,
					moduleName: vm.q.moduleName,
					tablePrefix: vm.q.tablePrefix
				};
				$.ajax({
					url: 'sys/generator/settings/save',
					type: 'POST',
					dataType: 'json',
					contentType: 'application/json',
					data: JSON.stringify(settings),
					success: function(r) {
						if (r.code === 0) {
							alert('设置已保存');
						} else {
							alert('保存失败：' + (r.msg || '未知错误'));
						}
					},
					error: function() {
						alert('保存失败：网络错误');
					}
				});
			}
	}
});
