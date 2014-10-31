<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" type="text/css" href="css/easyui.css">
<link rel="stylesheet" type="text/css" href="css/icon.css">
<link rel="stylesheet" type="text/css" href="css/demo.css">

<script type="text/javascript" src="js/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="js/jquery.easyui.min.js"></script>
<!-- -->
<script type="text/javascript" src="js/main.js"></script>

<script type="text/javascript">
var rootExpanded = false;
$(document).ready(function(){
	$('#easyui-treegrid').treegrid({
		/* onClick:function(node){
			document.getElementById('nodeTextArea').value = node.value;
		} */
		'onLoadSuccess':function(row, data) {
			if (row == null) {
				rootExpand();
			}
		}
		
	});
	$('#saveBtn').click(function(event) {
		var node = $('#easyui-treegrid').treegrid('getSelected');
		if (node == null) {
			alert('请选中一个节点');
			return false;
		}
		var params = {
		        nodeId: node.path,
		        value: document.getElementById('nodeTextArea').value
		    };
		$.ajax({    
		    url:'saveConfig.htm?'+$.param(params),    
		    type:'post',    
		    cache:false,
		    success:function(data) {
		        if(data =="true" ) {
		            alert("修改成功!");
		        } else {
		        	alert("修改失败!");
		        }
		     },    
		     error:function (XMLHttpRequest, textStatus, errorThrown) {
            	alert("修改异常!");
             }    
		});  
		
	});
	/* alert($('#easyui-treegrid').treegrid('getRoot'));
	$('#easyui-treegrid').treegrid('expandAll'); */
})

function rootExpand() {
	if (rootExpanded == false) {
		$('#easyui-treegrid').treegrid('expand',$('#easyui-treegrid').treegrid('getRoot').id);
		rootExpanded = true;
	}
}

function openAddDlg() {
	var node=$("#easyui-treegrid").treegrid("getSelected");
	if(node==null){
		alert("请选择父节点!");
		return;
	}
	$('#dlg').dialog('open');
	$("#nodeKeyText").focus();
}
function add(){
	var nodeKey=$("#nodeKeyText").val().trim();
	var nodeValue = $("#nodeValueText").val().trim();
	if(nodeKey==""){
		alert("请输入配置项！");
		$("#nodeKeyText").focus();
		return;
	}
	var pNode=$("#easyui-treegrid").treegrid("getSelected");
	$.ajax({
		type:"POST",
		url:"addConfig.htm",
		data:{pNodeVirtualPath:pNode.id,key:nodeKey,value:nodeValue},
		dataType:"text",
		success:function(msg){
			if (msg == 'true') {
				$("#easyui-treegrid").treegrid("append",{
					parent:pNode.id,
					data: [{
						text:nodeKey,
						value:nodeValue
					}]
				});
				$("#nodeKeyText").val("");
				$("#nodeValueText").val("");
				$("#easyui-treegrid").treegrid("reload",pNode.id);
				pNode.parent = "true";
				$('#dlg').dialog('close');
			} else {
				alert('操作失败!');
			}
		},
		error:function (XMLHttpRequest, textStatus, errorThrown) {
        	alert("操作异常!");
         }  
	});
}

function openModifyDlg() {
	var node=$("#easyui-treegrid").treegrid("getSelected");
	if(node==null){
		alert("请选择要修改的节点!");
		return;
	}
	$('#dlg-modify').dialog('open');
	$("#nodeKeyText4Modify").val(node.text);
	$("#nodeValueText4Modify").val(node.value);
	$("#nodeValueText4Modify").focus();
}

function update(){
	var nodeValue = $("#nodeValueText4Modify").val().trim();
	var node=$("#easyui-treegrid").treegrid("getSelected");
	$.ajax({
		type:"POST",
		url:"saveConfig.htm",
		data:{nodeVirtualPath:node.id,value:nodeValue},
		dataType:"text",
		success:function(msg){
			if (msg == 'true') {
				$('#easyui-treegrid').treegrid('update',{
					id: node.id,
					row: {
						value: nodeValue
					}
				});
				
				$("#nodeKeyText4Modify").val("");
				$("#nodeValueText4Modify").val("");
				$('#dlg-modify').dialog('close');
			} else {
				alert('操作失败!');
			}
		},
		error:function (XMLHttpRequest, textStatus, errorThrown) {
        	alert("操作异常!");
         }  
	});
}


function del() {
	var node=$("#easyui-treegrid").treegrid("getSelected");
	if (node == null) {
		alert("请选择要删除的节点!");
		return false;
	}
	if (node == $("#easyui-treegrid").treegrid("getRoot")) {
		alert("不能删除根节点!");
		return false;
	}
	
	if (node.parent == "true") {
		alert("不能直接删除父节点!");
		return false;
	}
	if (confirm("确认删除该配置节点 (" + node.text + ")吗 ????????? (此操作不能回复!!!!!!!)")) {
		$.ajax({
			type:"POST",
			url:"removeConfig.htm",
			data:{nodeVirtualPath:node.id},
			dataType:"text",
			success:function(msg){
				if (msg == 'true') {
					var pNode = $("#easyui-treegrid").treegrid("getParent", node.id);
					$("#easyui-treegrid").treegrid("reload",pNode.id);
					var nodes = $("#easyui-treegrid").treegrid("getChildren",pNode.id)
					if (nodes.length == 0) {
						pNode.parent = "false";
					}
				} else {
					alert('操作失败!');
				}
			},
			error:function (XMLHttpRequest, textStatus, errorThrown) {
	        	alert("操作异常!");
	         }  
		});
	}
	
}

</script>

<title>Moreopen Config</title>
</head>
<body class="easyui-layout">


	<!-- 北面区域 -->
	<div data-options="region:'north',border:true,split:true" style="height:25px;background: url('images/layout-browser-hd-bg.gif');">
		<b>Moreopen Config</b>
		欢迎您,${sessionScope.user.name}&nbsp;<a href="logout.htm">退出</a>&nbsp;
		<input type="hidden" id="requestUrl"> 
	</div>
	
	<!-- 左边区域 -->
	<div data-options="region:'west',split:true,title:'导航菜单'" style="width:800px;padding:10px;">
			<!-- 左边的树-->
			<!--ul 
				id="easyui-tree" 
				class="easyui-tree" 
				url="/config/getConfigItems.htm"
				lines="true"
			>
			</ul-->
			
		<table id="easyui-treegrid"
			url="/config/getConfigItems.htm"
			rownumbers="true" 
			idField="id" 
			treeField="text"
			striped="true"
			fit="true"
			nowrap="false">
			<thead>
				<tr>
					<th field="text" align="left">key</th>
					<th field="value" align="left">value</th>  
				</tr>
			</thead>
		</table>
			
	</div>
	
	<!-- 中间区域 -->
	<div data-options="region:'center',border:false">
		<input type="button" value="修改" onclick="openModifyDlg()"/>
		&nbsp;&nbsp;<input type="button" onclick="openAddDlg()" value="新增"/>
		&nbsp;&nbsp;<input type="button" onclick="del()" value="删除"/>
	</div>
	
	<!-- 添加配置项的dialog -->
		<div id="dlg" class="easyui-dialog" title="&nbsp;&nbsp;添加配置项" style="width:450px;height:250px;padding:10px"
				data-options="
					iconCls: 'icon-add',
					toolbar: '#dlg-toolbar',
					buttons: '#dlg-buttons',
					closed: true,
					modal: true
				">
				<table>
					<tr>
						<td align="right">配置项：</td>
						<td><input id="nodeKeyText" type="text" style="width:300px"/></td>
					</tr>
					<tr>
						<td align="right" valign="top">配置值：</td>
						<td><textarea id="nodeValueText" style="width: 300px;height: 100px"></textarea>
					</tr>
				</table>
		</div>
		
		<div id="dlg-buttons">
			<a href="javascript:void(0)" class="easyui-linkbutton" onclick="add()">Save</a>
			<a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dlg').dialog('close')">取消</a>
		</div>
		
	<!-- 修改配置项的dialog -->
		<div id="dlg-modify" class="easyui-dialog" title="&nbsp;&nbsp;修改配置" style="width:450px;height:250px;padding:10px"
				data-options="
					iconCls: 'icon-edit',
					toolbar: '#dlg-toolbar',
					buttons: '#dlg-modify-buttons',
					closed: true,
					modal: true
				">
				<table>
					<tr>
						<td align="right">配置项：</td>
						<td><input id="nodeKeyText4Modify" type="text" style="width:300px" disabled="disabled"/></td>
					</tr>
					<tr>
						<td align="right" valign="top">配置值：</td>
						<td><textarea id="nodeValueText4Modify" style="width: 300px;height: 100px"></textarea>
					</tr>
				</table>
		</div>
		
		<div id="dlg-modify-buttons">
			<a href="javascript:void(0)" class="easyui-linkbutton" onclick="update()">Save</a>
			<a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dlg-modify').dialog('close')">取消</a>
		</div>
	
	<!-- 南边区域 -->
	<div data-options="region:'south',split:true" style="height:25px;background:url('images/layout-browser-hd-bg.gif');padding:0px;text-align:center">
		版权所有，侵权不究 &copy;config.moreopen
	</div>
	 
</body>
</html>
<script>

</script>