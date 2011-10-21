<?php

$product = $_POST['product'];
$quantity = $_POST['quantity'];
$from = $_POST['from'];
$to = $_POST['to'];
$type = $_POST['type'];
//$number = $_POST['number'];

$info = "\nWpis umieszczony w bazie: \nProdukt: ".$product." o ilosci: ".$quantity." przeniesiono z ".$from." do ".$to;

$file = "baza.txt"; 
$fp = fopen($file, "a"); 
flock($fp, 2); 
fwrite($fp, $info); 
flock($fp, 3); 
$ok = fclose($fp);



//echo $info;
if ($ok) {
	echo '	
		<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		</head>
		
		<script type="text/javascript">
			function showLocalStorage() {
				for (var i=0, keys = localStorage.length; i < keys; ++i){
					var key = localStorage.key(i);
					alert(key + localStorage.getItem(key));
				}
			}
		
		//window.parent.nazwafunkcji
		parent.send_next_entry();
		
		
		</script>
		
		<body>
			<div class="tekst">'.$info.'</div>
		</body>
		 
		</html>
	'; 
}
else {
	echo '	
		<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		</head>
		
		<script type="text/javascript">
			alert("something went wrong");
		</script>
		
		<body>
			<div class="tekst"> jakistam tekst</div>
		</body>
		 
		</html>
	'; 

}

?>