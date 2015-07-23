define(["dialog","report/edit/canvas/table-setting/other-setting/other-setting-model","report/edit/canvas/table-setting/other-setting/other-setting-template"],function(a,b,c){return Backbone.View.extend({events:{"click .j-others-operate":"getOtherSettingData"},initialize:function(a){this.model=new b({canvasModel:a.canvasView.model,reportId:a.reportId}),this.model.set("compId",this.$el.find(".j-comp-setting").attr("data-comp-id"))},getOtherSettingData:function(){var a=this;a.model.getOtherSettingData(function(b){a._openOtherSettingDialog(b)})},destroy:function(){this.stopListening(),this.model.clear({silent:!0}),delete this.model,this.$el.unbind()},_openOtherSettingDialog:function(b){var d,e=this;d=c.render(b),a.showDialog({title:"其他操作",content:d,dialog:{width:350,height:270,resizable:!1,buttons:[{text:"提交",click:function(){e._saveOtherSettingInfo($(this))}},{text:"取消",click:function(){$(this).dialog("close")}}]}})},_saveOtherSettingInfo:function(a){var b=this,c={},d=$(".data-format-black").find('input[type="checkbox"]');d.each(function(){var a=$(this).attr("name"),b=$(this).is(":checked");c[a]=b?"true":"false"}),b.model.saveOtherSettingData(c,function(){a.dialog("close"),b._setRichSelect(c)})},_setRichSelect:function(a){var b,c=this,d=window.dataInsight.main.canvas,e=d.model,f=e.$reportVm,g=e.reportJson.entityDefs,h=c.model.get("compId"),i=$($(".active").children()[0]),j=f.find(".j-component-item").filter("[data-comp-id="+h+"]"),k=$(i.children()[0]).children()[0];if(!k)return d.model.saveJsonVm(function(){d.showReport()}),void 0;b=$(k).attr("data-o_o-di");var l=b.indexOf("rich-select");if("false"===a.canChangedMeasure){if(l>0){i.children()[0].remove(),j.height(j.height()-37),i=$($(j).children()[0]),i.children()[0].remove(),delete $.getTargetElement(h,g).vuiRef.richSelect;for(var m=0,n=0,o=g.length;o>m;m++)if(g[m].id===b){n=m+1;break}n&&(e.reportJson.entityDefs=g.slice(0,n-1).concat(g.slice(n)))}}else if(0>l){b="snpt."+h+"-vu-table-rich-select";var p=['<div class="di-o_o-line">','<div class="" data-o_o-di="',b,'"></div>',"</div>"].join(""),q={clzType:"VUI",clzKey:"RICH_SELECT",id:b,compId:h};i.prepend(p),j.height(j.height()+37),i=$($(j).children()[0]),i.prepend(p),e.reportJson.entityDefs.push(q);var r=$.getTargetElement(h,g);r.vuiRef.richSelect=b}d.model.saveJsonVm(function(){d.showReport()})}})});