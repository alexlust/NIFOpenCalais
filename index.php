<?php
 
 header('Content-Type: text/plain');
 
 
require('opencalais.php');
$apikey = "hyh9hhy2eghaqcub5kdcs3ae";
$url = 'http://library.thinkquest.org/4034/polo.html';
$oc = new OpenCalais($apikey, $url); 




$text = $oc->getCalaisResult();





$qs = 'input-type=text&nif=true&input=' . $text;

$stemmer = "http://nlp2rdf.lod2.eu/demo/NIFStemmer";
//$parser = ARC2::getRDFXMLParser();
//$parser->parse($stemmer);
//$stemmertriples = $parser->getTriples();

  ARC2::inc('Reader');
  $reader = new ARC2_Reader('', $this);
  $reader->setHTTPMethod('POST');
  $reader->setCustomHeaders("Content-Type: application/x-www-form-urlencoded");
  
  echo $stemmer.$qs;
  $reader->setMessageBody($qs);
  $reader->activate($stemmer);
  $r = '';
  while ($d = $reader->readStream()) {
    $r .= $d;
  }
  $reader->closeStream();
  
$dateiname = "output_nif.rdf"; 
$handler = fOpen($dateiname , "w+");
fWrite($handler , $r);
fClose($handler); 
  
// echo $r;






/*


$stanford = "http://nlp2rdf.lod2.eu/demo/NIFStanfordCore?input-type=text&nif=true&input=".urlencode($text);
$parser = ARC2::getRDFXMLParser();
$parser->parse($stanford);
$stanfordtriples = $parser->getTriples();
$alltriples = array_merge($stanfordtriples, $stemmertriples);
$ser = ARC2::getTurtleSerializer();
$output = $ser->getSerializedTriples($alltriples);
echo $output;

*/



















/*
$alltriples = array();
      
        //parsing
        $time_start = microtime(true);
        $parser = ARC2::getRDFXMLParser();
        $parser->parse($unparsed);
        $triples = $parser->getTriples();
        $alltriples = array_merge($alltriples, $triples);
        $time_end = microtime(true);
        $time_arc2_needed = round ($time_end - $time_start,2);
       
       
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
echo $output;
*/