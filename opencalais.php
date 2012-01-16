<?php

/**
* Open Calais Tags
* Last updated 1/16/2012
* Copyright (c) 2012 Dan Grossman
* http://www.dangrossman.info
*
* Please see http://www.dangrossman.info/open-calais-tags
* for documentation and license information.
*/

class OpenCalaisException extends Exception {}

class OpenCalais {

    private $api_url = 'http://api.opencalais.com/enlighten/rest/';
    private $api_key = 'hyh9hhy2eghaqcub5kdcs3ae';

    public $contentType = 'text/html';
    public $outputFormat = 'XML/RDF';
    public $getGenericRelations = true;
    public $getSocialTags = true;
    public $docRDFaccessible = true;
    public $allowDistribution = true;
    public $allowSearch = true;
    public $externalID = '';
    public $submitter = '';

    private $document = '';
	private $urli ="http://www.yesnet.yk.ca/schools/projects/renaissance/marcopolo.html";
    private $entities = array();

    public function OpenCalais($api_key, $link) {
        if (empty($api_key)) {
            throw new OpenCalaisException('An OpenCalais API key is required to use this class.');
        }
        $this->api_key = $api_key;
		$this->urli = $link;
    }

   
    private function getParamsXML() {

        $types = array();
        if ($this->getGenericRelations)
            $types[] = 'GenericRelations';
        if ($this->getSocialTags)
            $types[] = 'SocialTags';
        
        $xml = '<c:params xmlns:c="http://s.opencalais.com/1/pred/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">';
        $xml .= '<c:processingDirectives ';
        $xml .= 'c:contentType="' . $this->contentType . '" ';
        $xml .= 'c:enableMetadataType="' . implode(',', $types) . '" ';
        $xml .= 'c:outputFormat="' . $this->outputFormat . '" ';
        $xml .= 'c:docRDFaccessible="' . ($this->docRDFaccessible ? 'true' : 'false') . '" ';
        $xml .= '></c:processingDirectives>';
        $xml .= '<c:userDirectives ';
        $xml .= 'c:allowDistribution="' . ($this->allowDistribution ? 'true' : 'false') . '" ';
        $xml .= 'c:allowSearch="' . ($this->allowSearch ? 'true' : 'false') . '" ';

        if (!empty($this->externalID))
            $xml .= 'c:externalID="' . htmlspecialchars($this->externalID) . '" ';

        if (!empty($this->submitter))
            $xml .= 'c:submitter="' . htmlspecialchars($this->submitter) . '" ';

        $xml .= '></c:userDirectives>';
        $xml .= '<c:externalMetadata></c:externalMetadata>';
        $xml .= '</c:params>';
        
        return $xml;

    }


  public function getCalaisResult() { //param $id, $text
  
  $url = $this->api_url;
  $qs = 'licenseID=' . urlencode($this->api_key);
  $qs .= '&paramsXML=' . urlencode($this->getParamsXML());
  
  
  $text = file_get_contents($this->urli);
  
  require_once( 'html2text.class.php');

 
  $h2t=new html2text();
  $h2t->auto_tolower=false;
  $text=$h2t->HtmlToText($text);

  $qs .= '&content=' . $text;
    
  return $this->getAPIResult($url, $qs);
}

  public function getAPIResult($url, $qs) {
  include_once('/arc/ARC2.php');	
  ARC2::inc('Reader');
  $reader = new ARC2_Reader('', $this);
  $reader->setHTTPMethod('POST');
  $reader->setCustomHeaders("Content-Type: application/x-www-form-urlencoded");
  $reader->setMessageBody($qs);
  $reader->activate($url);
  $r = '';
  while ($d = $reader->readStream()) {
    $r .= $d;
  }
  $reader->closeStream();
 
$dateiname = "output_calais.rdf"; 
$handler = fOpen($dateiname , "w+");
fWrite($handler , $r);
fClose($handler); 
return $r;
	}
	
}