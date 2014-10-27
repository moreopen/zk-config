/**$('#easyui-tree').tree({
	onClick:function(node){
		alert("aaa");
		if($(this).tree('isLeaf',node.target)){//如果是叶节点，点击就在右边添加tab
			
			if(!$('#easyui-tabs').tabs('exists',node.text)){//如果这个标题的tab还不存在，就添加
				$('#easyui-tabs').tabs('add',{
						title: node.text,
						content: createFrame(node.attributes.url),  // the new content URL
						closable: true,
						cache:false
					});
			}else{	//如果已经存在，就让其处于选中状态
				$('#easyui-tabs').tabs('select',node.text).selected;
			}
		}
	}
});


function tabRightClick()
{
	$("#easyui-tabs").tabs({
		onContextMenu:function(e,title,index){
			$('#mm').menu('show', {
				left: e.pageX,
				top: e.pageY
			});
		}
	});
}

**/
//绑定右键菜单事件
