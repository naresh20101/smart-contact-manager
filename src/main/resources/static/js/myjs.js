console.log("Script file");
const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};


const search=()=>{
	//console.log("searching");
	let query=$("#search").val();
	if(query=="")
	{
		$(".search-result").hide();
	}
	else{
		let url = `http://localhost:8080/user/search/${query}`;
		fetch(url).then(response => {
			return response.json();
		}).then(data => {

			let text = `<div class="btn-group">`
			data.forEach(contact => {
				text += `<a href="/user/${contact.cId}/contact" class="btn btn - light" data-mdb-color="dark">${contact.name}</a>`
			});

			text += `</div>`

			$(".search-result").html(text);

			$(".search-result").show();
		});


		//$(".search-result").show();
	}
};