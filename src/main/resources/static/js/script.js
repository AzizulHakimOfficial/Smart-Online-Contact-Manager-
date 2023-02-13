
const toggleSidebar = () =>{
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};

var timePerriodInMs=4000;
        setTimeout(function(){
            document.getElementById("text-message").style.display="none";
        },
        timePerriodInMs);  
          
const search = () => {
	let query=$("#search-input").val()
	if(query==''){
		$(".search-result").hide();
	}else{
		let url=`http://localhost:9292/search/${query}`;
		
		fetch(url).then((response) => {
			return response.json();
		}).then((data) =>{
			
			let text=`<div class='list-group'>`
			
			data.forEach((contact) =>{
				text+=`<a href='/user/${contact.id}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
			});
			
			text+=`</div>`;
			$(".search-result").html(text);
			$(".search-result").show();
		});
	}
};