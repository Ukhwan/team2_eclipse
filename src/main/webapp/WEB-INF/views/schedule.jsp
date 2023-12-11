<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- 부트스트랩 CSS -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
<link rel="stylesheet" href="/css/test.css">

<!-- 부트스트랩 JavaScript (Popper.js와 jQuery 포함) -->
<script src="https://code.jquery.com/jquery-3.5.1.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
<script type="text/javascript" src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=dsbc03s9xu"></script>
<script type="text/javascript" src="https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=dsbc03s9xu&submodules=geocoder"></script>

    <title>Document</title>
</head>
<body>
    <div class="container">
        <div class="sidebar">
            <div class="sidebar1">
                <a href="" target="_self">
                    
                    <img src="/images/여행로고.PNG" class="logo">
                </a>
            </div>
            <div class="sidebar2">
                <button class="sidebtn1 btn-outline-info">
                    STEP 1<br>
                    날짜 확인
                </button>
            </div>
            <div class="sidebar3">
                    <button class="sidebtn2 btn-outline-info">    STEP 2<br>
                                장소 선택
                    </button>
            </div>
            <div class="sidebar4">
                <button class="sidebtn3 btn-outline-info">    STEP 3<br>
                            숙소 선택
                </button>
        </div>
        </div>
        <div class="content">
            <div class="date">
                2023.12.03~2023.12.05
            </div>
            <div class="btndiv">
                <button class="selbtn btn-outline-info">여행 장소 선택</button>
                <button class="rebtn btn-outline-info">새로운 장소 등록</button>
            </div>
            <div><input type="text" id="de" placeholder="장소명을 입력하세요" onkeydown="handleEnterKey(event)"></div>
            <div>
                <button class="cbtn1 btn-outline-primary" id="zz" onclick="initMap()">추천</button>
                <button class="cbtn2 btn-outline-info">명소</button>
                <button class="cbtn3 btn-outline-info">식당</button>
                <button class="cbtn4 btn-outline-info">카페</button>
            </div>
            
            <div class="traveld">
                <div class="travelimg">
                <img src="" id="fm">
            </div>
            <div class="traveltext">
                <div class="linea" id="title"><br></div>
                <div class="lineb" id="addr"><br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
        </div>
        <div class="sel">
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
            <div class="traveld">
                <div class="travelimg">
                <img src="/images/image-AiH.png">
            </div>
            <div class="traveltext">
                <div class="linea">이름<br></div>
                <div class="lineb">종류 주소<br></div>
                <div class="linec">하트 별</div>
            </div>
            </div>
        
        </div>
        <div class="map" id="map"></div>
    </div>
</body>
<script>

var map = null;


function initMap() {
	if(map==null){
         map = new naver.maps.Map('map', {
             center: new naver.maps.LatLng(33.4996, 126.5312),
             zoom: 10
         });
	} 
	}
function handleEnterKey(event) {
    if (event.key === "Enter") {
        submitForm(); // 엔터 키를 눌렀을 때 submitForm 함수 호출
    }
}

// 폼 제출 함수
function submitForm() {
    // 여기에 입력값을 처리하는 코드를 추가하면 됩니다.
    var inputValue = document.querySelector('input[type="text"]').value;
   $.ajax({
	 type:'get',
	 dataType:'json',
	 url:'search?addr='+inputValue,
	 cache:false,
	 processData:true,
	 success:function(res){
		moveMap(res.x,res.y);
		tour(res.x,res.y);
	 },
	 error:function(err){
			alert('error: '+err.status);
	 }
	   
   })
    
}
function moveMap(lat, len) {
    var mapOptions = {
        center: new naver.maps.LatLng(len, lat),
        zoom: 10,
        mapTypeControl: true
    };
    map = new naver.maps.Map('map', mapOptions);
}
function tour(lat,len){
	var x=lat;
	var y=len;

	$.ajax({
		 type:'get',
		 dataType:'json',
		 url:'tour?x='+x+'&y='+y,
		 cache:false,
		 processData:true,
		 success:function(res){
			 
			/*   $('#title').text(res.title);
	          $('#addr').text(res.addr1);
	          $('#fm').attr('src', res.firstimage); 
 */			 
	alert('성공');	
	            
	            
		 },
			 error:function(err){
					alert('error: '+err.status);
			 }
	})
}    


</script>
</html>