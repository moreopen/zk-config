
$(document).ready(function(){
	//InitLeftMenu();
	//onClickRow();
})
function InitLeftMenu(){
	$.getJSON("/monitor/getEditMenu.htm",function(json){
		//var treeData=eval("("+json.data+")");
		$("#easyui-treegrid").treegrid("loadData",json);
	});
}





function collapse(){
	var node = $('#easyui-tree').tree('getSelected');
	$('#easyui-tree').tree('collapse',node.target);
}
function expand(){
	var node = $('#easyui-tree').tree('getSelected');
	$('#easyui-tree').tree('expand',node.target);
}