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
            queryFormSchema: true,
            serviceAndImpl: false,
            mybatisXML: false,
            useYesOrNoEnum: true,
            useEnabledStatusEnum: true,
		}
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
                // 借助 !! 操作符转换为 boolean 型
                + '&queryFormSchema=' + !!vm.q.queryFormSchema
                + '&serviceAndImpl=' + !!vm.q.serviceAndImpl
                + '&mybatisXML=' + !!vm.q.mybatisXML
                + '&useYesOrNoEnum=' + !!vm.q.useYesOrNoEnum
                + '&useEnabledStatusEnum=' + !!vm.q.useEnabledStatusEnum
            ;
		}
	}
});

