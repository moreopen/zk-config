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
$(document).ready(function(){
	$('#easyui-tree').tree({
		onClick:function(node){
			document.getElementById('nodeTextArea').value = node.attributes.value;
		}
	});
	$('#saveBtn').click(function(event) {
		var node = $('#easyui-tree').tree('getSelected');
		if (node == null) {
			alert('请选中一个节点');
			return false;
		}
		var params = {
		        nodeId: node.id,
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
})

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
	<div data-options="region:'west',split:true,title:'导航菜单'" style="width:250px;padding:10px;">
			<!-- 左边的树-->
			<ul 
				id="easyui-tree" 
				class="easyui-tree" 
				url="/config/getConfigItems.htm"
				lines="true"
			>
			</ul>
	</div>
	
	<!-- 中间区域 -->
	<div data-options="region:'center',border:false">
		<textarea id="nodeTextArea" style="width: 500px;height: 100px"></textarea>
		<br/><br/><input id="saveBtn" type="button" value="提交"/>
	</div>
	
	<!-- 南边区域 -->
	<div data-options="region:'south',split:true" style="height:25px;background:url('images/layout-browser-hd-bg.gif');padding:0px;text-align:center">
		版权所有，侵权不究 &copy;config.moreopen
	</div>
	 
</body>
</html>