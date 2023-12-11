<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>
<button id="submit()"></button>
<P>  The time on the server is ${serverTime}. </P>
</body>
</html>

<script>
function submit(){
	$ajax.({
		type:'get',
		dataType:'json',
		url:'code',
		cache:fase,
		processData:true,
		success:function(res){
			
			
		},
		error:function(err){
			alert('error: 'err.status);
			
		}
		
	})
	
	
	
}
</script>