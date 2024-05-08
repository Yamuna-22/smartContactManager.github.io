console.log("this is script file");
const toggleSidebar= () => {
    //if side bar is visble make it invisible , else make it visible
 if($(".sidebar").is(":visible")){
    $(".sidebar").css("display","none");
    $(".content").css("margin-left","0%");
 }else{
    $(".sidebar").css("display","block");
    $(".content").css("margin-left","20%");
 }
};


// for search functionality in show contacts
const search=()=>{
 console.log("Searching..")
 let query=$("#search-input").val();
 if(query==""){
   $(".search-result").hide();

 }else{
   //search
   console.log(query);
   //sending request to server
   //to print the respose , use data
   let url=`http://localhost:8080/search/${query}`;
   fetch(url).then((response)=>{
     return response.json();
   })
   .then((data)=>{
    
    let text=`<div class='list-group'>`;
     data.forEach((contact) => {
      text+=`<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'>${contact.name}</a>`
     });

       text +=`</div>`;
       $(".search-result").html(text);
       $(".search-result").show();

   });
   $(".search-result").show();
 }
};


//first request to server to create order
 
const paymentStart=()=>{
  console.log("Payment begun");
  let amount=$("#Payment_field").val()
  console.log(amount);
  if(amount=='' || amount==null){
    alert("Amount is required !!");
    return;
  }


  //we will use ajax from JS to send request to server to create order

  
};