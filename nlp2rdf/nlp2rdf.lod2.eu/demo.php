<?php
include("ARC2/ARC2.php");

$prefix = (isset($_REQUEST['prefix'] ))?$_REQUEST['prefix'] :"http://prefix.given.by/theClient#";
$urirecipe = (isset($_REQUEST['urirecipe'] ))?$_REQUEST['urirecipe'] :"offset";
$text = $_REQUEST['text'];

?>

<html>
<title>NLP2RDF demo</title>
<body>
<?php  ; 
//print_r($_REQUEST)	;
?>
	
	<h1>NLP2RDF demo</h1>
This web site show cases the available nif webservices.
Note that the list displayed here is not complete. 
More tools will be added here: <a href="http://nlp2rdf.org/implementations"> http://nlp2rdf.org/implementations</a> 
or in the <a href="http://code.google.com/p/nlp2rdf/source/browse/#hg%2Fimplementation"> NLP2RDF Google Code</a> repository. 
The code for this demo is available <a href="http://code.google.com/p/nlp2rdf/source/browse/nlp2rdf.lod2.eu/demo.php">here</a>.
<!--The server that hosts the services is really slow. (We measured it and some of our laptops are 30 times faster than the server).-->
<br>




	
<h3>Post text into the field below: (<a href="demo.php">reset</a>)</h3>

<form>
	<div>
		<div style="float:left;" >
<textarea name="text" cols = 45 rows = 6><?php 
if (isset($_REQUEST['text'] )) {
	echo $_REQUEST['text'] ;
} else { 
		echo "President Obama on Monday will call for a new minimum tax rate for individuals making more than $1 million a year."  ;
}  ?></textarea><br />
Select action:<br />
<input type="radio" name="action" value="merge" checked />display and merge<br />
<input type="radio" name="action" value="validate"  disabled  /><del>validate</del><br />
<input type="radio" name="action" value="summarize"  disabled /><del>summarize</del><br />
URI Recipe:
<input type="radio" name="urirecipe" value="offset"  checked />offset <input type="radio" name="urirecipe" value="context-hash" />context-hash<br />
Prefix: 
<input type="text" name="prefix"  size = 30 value = "<?php echo $prefix; ?>" /><br />
<input type="submit" value="Submit" />
</div>
<div style="float:left;">
Select tools:<br />
<ul>
Available: (currently called sequentially, not parallel)<br>
<?php 
$serviceCheckboxes=array(
"<a href=\"http://nlp2rdf.org/implementations/snowballstemmer\" target=_blank >Snowball Stemmer</a>"=>"http://nlp2rdf.lod2.eu/demo/NIFStemmer", 
"<a href=\"http://nlp2rdf.org/implementations/stanford-corenlp\" target=_blank >Stanford CoreNLP</a>"=>"http://nlp2rdf.lod2.eu/demo/NIFStanfordCore",
"<a href=\"http://code.google.com/p/nlp2rdf/source/browse/#hg%2Fimplementation%2Fopennlp\" target=_blank >OpenNLP</a>"=>"http://nlp2rdf.lod2.eu/demo/NIFOpenNLP" ,
"<a href=\"https://github.com/kenda/nlp2rdf.MontyLingua\" target=_blank >MontyLingua (Python)</a>"=>"http://nlp2rdf.lod2.eu/demo/NIFMontyLingua" ,
"<a href=\"https://github.com/robbl/node-dbpedia-spotlight-nif\" target=_blank >DBpedia Spotlight (node-js)</a>"=>"http://nlp2rdf.lod2.eu/demo/NIFDBpediaSpotlight" 
//""=>"", 
);
$first = true;
foreach($serviceCheckboxes as $key=>$value ){
		$checked = "";
		if(isset($_REQUEST['service']) && in_array($value, $_REQUEST['service'])) {
			$checked = "checked";
		}else if(!isset($_REQUEST['service']) && $first){
			$checked = "checked";
		}
		echo "<li><input type=\"checkbox\" name=\"service[]\" value=\"$value\" $checked />$key<br /></li>\n";
		$first = false;
}
?>

	
<li>Other NIF service: <input type="text" name="service[]" /><br /></li>
Coming soon (not deployed as demo currently): <br>
	<li><a href="https://bitbucket.org/gruenerkaktus/uimanif/overview" target="_blank"> UIMA</a></li>
	<li><a href="https://bitbucket.org/mack/mallet2nif" target="_blank">Mallet</a></li>
	<li><a href="https://bitbucket.org/d_cherix/gan/overview" target="_blank">Gate ANNIE</a> </li>
</ul>
</div>
<br style="clear:both;"/>
</div>
</form>

<?php
$meta = ""; 
$output = "";
$alltriples = array();
foreach (@$_REQUEST['service'] as $service){
	if(trim($service) == "") continue;
	//retrieve
	$time_start = microtime(true);
	$uri = $service."?input-type=text&nif=true&prefix=".urlencode($prefix)."&urirecipe=$urirecipe&input=".urlencode($_REQUEST['text']); 
	$data = file_get_contents($uri);
	$time_end = microtime(true);
	$time_service_needed = round ($time_end - $time_start,2);
	
	//parsing
	$time_start = microtime(true);
	$parser = ARC2::getRDFXMLParser();
	$parser->parse($prefix, $data);
	$triples = $parser->getTriples();
	$alltriples = array_merge($alltriples, $triples);
	$time_end = microtime(true);
	$time_arc2_needed = round ($time_end - $time_start,2);
	
	$meta .= "
	<h4>Sevice was $service:</h4>
	<ul>
			<li>NLP component needed: $time_service_needed seconds.</li>
			<li>ARC2 RDF Parser overhead: $time_arc2_needed seconds.</li>
			<li><a href=\"$uri\" >request url</a> </li>
			<li>Text size: ".strlen($_REQUEST['text'])."</li>
			<li>Triples: ".count($triples)."</li>
		</ul>";
	
}
$ns = array(
       'rdf' => 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
       'rdfs' => 'http://www.w3.org/2000/01/rdf-schema#',
       'owl' => 'http://www.w3.org/2002/07/owl#',
       'sso' => 'http://nlp2rdf.lod2.eu/schema/sso/',
       'str' => 'http://nlp2rdf.lod2.eu/schema/string/',
       'topic' => 'http://nlp2rdf.lod2.eu/schema/topic/',
       'error' => 'http://nlp2rdf.lod2.eu/schema/error/',
       'olia' => 'http://purl.org/olia/olia.owl#',
       'olia-top' => 'http://purl.org/olia/olia-top.owl#',
       'olia_system' => 'http://purl.org/olia/system.owl#',
       'penn' => 'http://purl.org/olia/penn.owl#',
       'penn-syntax' => 'http://purl.org/olia/penn-syntax.owl#',
       'stanford' => 'http://purl.org/olia/stanford.owl#',
       'brown' => 'http://purl.org/olia/brown.owl#');
		
$ser = ARC2::getTurtleSerializer(array('ns' => $ns));
$output = $ser->getSerializedTriples($alltriples);
?>

<h3>Merged result:</h3>
<?php echo $meta;?>
Merged Output:<br>
<textarea cols = 200 rows = 20 ><?php echo $output;?></textarea>

<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</body>

</html>
