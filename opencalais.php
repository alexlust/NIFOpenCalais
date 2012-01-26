<?php

class OpenCalais {

    private $api_url = 'http://api.opencalais.com/enlighten/rest/';
    private $api_key = 'hyh9hhy2eghaqcub5kdcs3ae';

    public $contentType = 'text/raw';
    public $outputFormat = 'text/N3'; //text/N3';//XML/RDF';
    public $getGenericRelations = true;
    public $getSocialTags = true;
    public $docRDFaccessible = true;
    public $allowDistribution = true;
    public $allowSearch = true;
    public $externalID = '';
    public $submitter = '';

    private $document = '';
	
    private $entities = array();

    public function OpenCalais($api_key) {
        if (empty($api_key)) {
            throw new OpenCalaisException('An OpenCalais API key is required to use this class.');
        }
        $this->api_key = $api_key;
	
    }

   
    private function getParamsXML() {

        $types = array();
        if ($this->getGenericRelations)
            $types[] = 'GenericRelations';
        if ($this->getSocialTags)
            $types[] = 'SocialTags';
        
        $xml = '<c:params xmlns:c="http://s.opencalais.com/1/pred/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">';
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


  public function getCalaisResult($text) { 
  
  $url = $this->api_url;
  $qs = 'licenseID=' . urlencode($this->api_key);
  $qs .= '&paramsXML=' . urlencode($this->getParamsXML());
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

return $r;
	}
	
}