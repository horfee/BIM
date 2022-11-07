import {
    IFCBUILDING, IFCPROJECT, IFCSITE, IFCZONE, IFCBUILDINGSTOREY, IFCSPACE, IFCSYSTEM, IFCPRODUCT, IFCUNITARYEQUIPMENT,
    IFCMATERIAL, IFCELEMENTASSEMBLY, IFCPOSTALADDRESS, IFCRELASSOCIATESCLASSIFICATION, IFCCLASSIFICATIONREFERENCE, IFCSIUNIT, IfcUnitEnum, 
    IFCMONETARYUNIT, IFCELEMENTQUANTITY, IFCTYPEOBJECT, IFCRELAGGREGATES, IFCELEMENT, IFCCONSTRUCTIONPRODUCTRESOURCE, IFCRELDEFINESBYPROPERTIES, 
    IfcRelDefinesByProperties, Handle, IfcObjectDefinition, IFCRELASSOCIATES,
    IFCRELASSIGNSTOGROUP,
    IfcProduct
    
  } from "web-ifc/web-ifc-api";
import * as XLSX from 'xlsx/xlsx.mjs';





async function fetchProperties( ifcApi, modelID, expressID, indirect = false, recursive = false, related) {
  const props = await ifcApi.getProperties(modelID, expressID, indirect, recursive);

  if ( related ) {
    //let lines = ifcApi.loader.ifcManager.ifcAPI.GetLineIDsWithType(modelID, IFCRELDEFINESBYPROPERTIES);
    //let lines2 = ifcApi.loader.ifcManager.ifcAPI.GetLineIDsWithType(modelID, IFCRELASSOCIATES);
    //let lines3 = ifcApi.loader.ifcManager.ifcAPI.GetLineIDsWithType(modelID, IFCRELASSOCIATESCLASSIFICATION);
    
    const toProcess = (related instanceof Array ? related : [related]).map( (related) =>  (
      {
        relation: related.relation, 
        vector: ifcApi.loader.ifcManager.ifcAPI.GetLineIDsWithType(modelID, related.type),
        checkFor: related.checkFor
      
    }));
    
    for( const process of toProcess) {
      if ( process.vector.size() > 0) props[process.relation] = [];
      for (let i = 0; i < process.vector.size(); i++) {
        let relID = process.vector.get(i);
        let rel = ifcApi.loader.ifcManager.ifcAPI.GetLine(modelID, relID);
        if ( (process.checkFor && rel[process.checkFor]?.value == expressID) || rel.RelatedObjects.some( (relID) => relID.value == expressID) ) {
          // if (!Array.isArray(rel.RelatedObject)) {
          //   props[process.relation].push(ifcApi.loader.ifcManager.ifcAPI.GetLine(modelID, rel.RelatedObject.value, true));
          // } else {
            const array = rel.RelatedObjects.map( (rel) => ifcApi.loader.ifcManager.ifcAPI.GetLine(modelID, rel.value, true));
            props[process.relation] = props[process.relation].concat(array);
          // }
        }
    }
    }

  }

  return props;
} IfcProduct

function getEmailFromHistory(history) {
  const person = history.OwningUser.ThePerson;
  const organization = history.OwningUser.TheOrganization;

  const email = (person?.Addresses && person?.Addresses[0]?.ElectronicMailAddresses[0]) ||
                (organization?.Addresses && organization?.Addresses[0]?.ElectronicMailAddresses[0]);
  
  if ( email ) return email;

  const givenName = person.GivenName?.value;
  const familyName = person.FamilyName?.value;
  const organizationName = organization.Name?.value;

  if ( givenName === undefined && familyName === undefined && organizationName === undefined ) {
    console.error("No primary key could be determined from %s", history);
    return "n/a";
  }
  
  return `${givenName} ${familyName} at ${organizationName}`;
  
}

function getPhoneFromHistory(history) {
  const person = history.OwningUser.ThePerson
  const organisation = history.OwningUser.TheOrganization

  const email = person?.Addresses && person?.Addresses[0]?.TelephoneNumbers[0] ||
                organisation?.Addresses && organisation?.Addresses[0]?.TelephoneNumbers[0];
  return email;
}

function getPostalAddressFromHistory(history) {
  const res = history.OwningUser?.ThePerson?.Addresses?.filter( (elt) => elt.type == IFCPOSTALADDRESS )[0] || 
              history.OwningUser?.TheOrganization?.Addresses?.filter( (elt) => elt.type == IFCPOSTALADDRESS )[0];
  return res;
}

function getCategoryFromHistory(history, context) {
  let allRoles = [];
  
  allRoles = allRoles.concat(history.OwningUser.Roles || []);
  allRoles =  allRoles.concat(history.OwningUser.ThePerson.Roles || []);
  allRoles =  allRoles.concat(history.OwningUser.TheOrganization.Roles || []);

  allRoles = allRoles.map( (role) => role.Role === "USERDEFINED" ? role.UserDefinedRole : role.Role );
  const result = Array.from(new Set(allRoles)).join(",");
  if ( result && result.length > 0 ) {
    context.pickLists["Category-Role"].push(result);
  }
  return result;
}

function getExtSystemFromHistory(history) {
  return history.OwningApplication.ApplicationFullName.value;
}

function getExtObjectFrom(data) {
  return data && data.type ? data.constructor.name : undefined;
}

function getInternalLocationFromOrganization(organization) {
  return organization?.Addresses && organization?.Addresses[0]?.InternalLocation;
}

function getDepartmentFromHistory(history) {
  const organization = history.OwningUser.TheOrganization;
  const department = getInternalLocationFromOrganization(organization) || 
                    history.OwningUser.TheOrganization.Name || "n/a";
  
        // department = self.get_internal_location_from_organisation(organisation)
        // if department:
        //     return department
        // last_department = None
        // for relationship in organisation.Relates:
        //     for related_organisation in relationship.RelatedOrganizations:
        //         department = self.get_internal_location_from_organisation(related_organisation)
        //         if department:
        //             last_department = department
        // if last_department:
        //     return last_department
        // return history.OwningUser.TheOrganization.Name or "n/a"
  return department;
}

function findAncestor(context, expressID, type) {
  if ( context.spatialStructure.flatten[expressID] ) return context.spatialStructure.flatten[expressID].find( elt => elt.type == type).expressID;
  // else : not flattened yet
  
  const _recursiveFlatten = function(topNode, expressID) {
    if ( topNode.expressID == expressID ) return [{type: topNode.type, expressID: expressID}];
    for(const child of topNode.children || []) {
      const res = _recursiveFlatten(child, expressID);
      if ( res.length > 0 ) return [{type: topNode.type, expressID: topNode.expressID}, ...res];
    }
    return [];
  }
  context.spatialStructure.flatten[expressID] = _recursiveFlatten(context.spatialStructure.hierarchy[0], expressID);
  return context.spatialStructure.flatten[expressID].find( elt => elt.type == type).expressID;
}

async function getContacts(ifcApi, modelID, context) {
  const rows  = [];

  const project = (await ifcApi.getAllItemsOfType(modelID, IFCPROJECT, true))[0];
  const projectProperties = await ifcApi.getProperties(modelID, project.expressID, false, true);
  const ownerHistory = projectProperties.OwnerHistory ? (projectProperties.OwnerHistory instanceof Array ? projectProperties.OwnerHistory : [projectProperties.OwnerHistory]) : "n/a";
  ownerHistory.forEach( (history) => {
    const email = getEmailFromHistory(history);
    const postalAddress = getPostalAddressFromHistory(history);
    const row = {
      Email: email,
      CreatedBy: getEmailFromHistory(history),
      CreatedOn: history.CreateDate ? new Date(history.CreationDate * 1000).toISOString() : new Date().toISOString(),
      Category: getCategoryFromHistory(history, context),
      Company: history.OwningUser.TheOrganization.Name || "n/a",
      Phone: getPhoneFromHistory(history) || "n/a",
      ExtSystem: getExtSystemFromHistory(history),
      ExtObject:  getExtObjectFrom(history.OwningUser),
      ExtIdentifier: history.OwningUser.ThePerson.Identification || history.OwningUser.ThePerson.Id || email,
      Department: getDepartmentFromHistory(history) || "n/a",
      OrganizationCode: history.OwningUser.TheOrganization.Identification || history.OwningUser.TheOrganization.Id || "n/a",
      GivenName: history.OwningUser?.ThePerson?.GivenName?.value || "n/a",
      FamilyName: history.OwningUser?.ThePerson?.FamilyName?.value || "n/a", 
      Street: postalAddress?.AddressLines ? (postalAddress.AddressLines instanceof Array ? postalAddress.AddressLines.join(", ") : postalAddress.AddressLines) : "n/a",
      PostalBox: postalAddress?.PostalBox || "n/a",
      Town: postalAddress?.Town || "n/a", 
      StateRegion: postalAddress?.Region || "n/a",
      PostalCode: postalAddress?.PostalCode || "n/a",
      Country: postalAddress?.Country || "n/a"
    }
    rows.push(row);
  });
    
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Contact", sheet: sheet};
}

function getCategoryFromObject(obj, context, pickList) {
  let classIdentification;
  let className;

  const assocations = (obj.HasAssociations || []).filter( (association) => association.type == IFCRELASSOCIATESCLASSIFICATION && association.RelatingClassification.type == IFCCLASSIFICATIONREFERENCE);
  const association = assocations[0];
  if ( association ) {
    classIdentification = assocation.RelatingClassification.ItemReference || association.RelatingClassification.Identification;
    className = association.RelatingClassification.Name;
  }
  let result;
  if ( classIdentification == undefined || className == undefined ) {
    console.error("The classification has invalid identification and name for %s", obj);
    classIdentification = "None";
    className = "None";
  } 
  
  result = `${classIdentification}:${className}`;
  context.pickLists[pickList].push(result);
  return result;

}

/*
for association in object.HasAssociations:
    if not association.is_a("IfcRelAssociatesClassification"):
        continue
    if not association.RelatingClassification.is_a("IfcClassificationReference"):
        continue
    if self.file.schema == "IFC2X3":
        class_identification = association.RelatingClassification.ItemReference
    else:
        class_identification = association.RelatingClassification.Identification
    class_name = association.RelatingClassification.Name
    break
if not class_identification or class_name:
    self.logger.error("The classification has invalid identification and name for %s", object)
result = "{}:{}".format(class_identification, class_name)
self.picklists[picklist].append(result)
return result

}*/

// async function getProjectNameFromBuilding(ifcApi, modelID, building, context) {
//   const project = findAncestor(context, building.expressID, "IFCPROJECT");
//   if ( project ) {
//     const props = await ifcApi.getProperties(modelID, project, false, false);
//     return props.Name?.value;
//   }
//   return "n/a";
// }

// async function getSiteNameFromBuilding(ifcApi, modelID, building, context) {
//   const site = findAncestor(context, building.expressID, "IFCSITE");
//   if ( site ) {
//     const props = await ifcApi.getProperties(modelID, site, false, false);
//     return props.Name?.value;
//   }
//   return "n/a";
// }

async function getProjectNameFromBuilding(ifcApi, modelID, building, context) {
  const projectProps = await getAncestorPropertiesOfType(ifcApi, modelID, building.expressID, context, "IFCPROJECT");
  return getAttributeOf(projectProps, "Name");
}

async function getSiteNameFromBuilding(ifcApi, modelID, building, context) {
  const siteProps = await getAncestorPropertiesOfType(ifcApi, modelID, building.expressID, context, "IFCSITE");
  return getAttributeOf(siteProps, "Name");
}

function getAttributeOf(object, attribute, defaultVal = "n/a") {
  if ( object ) {
    return object[attribute]?.value || defaultVal;
  }
  return defaultVal;
}

async function getAncestorPropertiesOfType(ifcApi,modelID, expressID, context, type, indirect = false, recursive = false) {
  const obj = findAncestor(context, expressID, type);
  if ( obj ) {
    return await ifcApi.getProperties(modelID, obj, indirect, recursive);
  }
  return await undefined;
}

async function getUnitsFromBuilding(ifcApi, modelID, building, context) {
  const project = await getAncestorPropertiesOfType(ifcApi, modelID, building.expressID, context, "IFCPROJECT", false, true);
  return project.UnitsInContext.Units;
}

function getUnitTypeFromUnits(units, type) {
  const unit = units.find( (unit) => unit.UnitType.value == type);
  if ( unit ) {
    if ( unit.type == IFCSIUNIT && unit.Prefix ) {
      return `${unit.Prefix.value}${unit.Name.value}`;
    } else {
      return unit.Name.value;
    }
  }
  return undefined;
}

function getMonetaryUnitFromUnits(units) {
  const unit = units.find( (unit) => unit.type == IFCMONETARYUNIT );
  if ( unit ) {
    return unit.Currency;
  }
  return "n/a";
}
  
function getAreaMeasurementFromBuilding(building) {
  const relationship = (building.IsDefinedBy || []).find( (rel) => rel.RelatingPropertyDefinition?.type == IFCELEMENTQUANTITY && rel.RelatingPropertyDefinition?.MethodOfMeasurement );
  if ( relationship ) {
    return relationship.RelatingPropertyDefinition.MethodOfMeasurement;
  }
  return "n/a";
}



async function getFacilities(ifcApi, modelID, context) {
  const buildings = await ifcApi.getAllItemsOfType(modelID, IFCBUILDING, true);
  const rows = [];
  for(const building of buildings) {
    const props = await fetchProperties(ifcApi, modelID, building.expressID, true, true, {relation: "HasAssociations", type: IFCRELASSOCIATESCLASSIFICATION});
    const units = await getUnitsFromBuilding(ifcApi, modelID, building, context) ;
    const site = await getAncestorPropertiesOfType(ifcApi, modelID, building.expressID, context, "IFCSITE");
    const project = await getAncestorPropertiesOfType(ifcApi, modelID, building.expressID, context, "IFCPROJECT");
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn: props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(props, context, "Category-Facility"),
      ProjectName: await getProjectNameFromBuilding(ifcApi, modelID, building, context),
      SiteName: await  getSiteNameFromBuilding(ifcApi, modelID, building, context),
      LinearUnits: getUnitTypeFromUnits(units, IfcUnitEnum.LENGTHUNIT),
      AreaUnits: getUnitTypeFromUnits(units, IfcUnitEnum.AREAUNIT),
      VolumeUnits: getUnitTypeFromUnits(units, IfcUnitEnum.VOLUMEUNIT),
      CostUnit: getMonetaryUnitFromUnits(units),
      AreaMeasurement: getAreaMeasurementFromBuilding(props),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExternalProjectObject: getExtObjectFrom(project),
      ExternalProjectIdentifier: getAttributeOf(project, "GlobalId"),
      ExternalSiteObject: getExtObjectFrom(site),
      ExternalSiteIdentifier: getAttributeOf(site, "GlobalId"),
      ExternalFacilityObject: getExtObjectFrom(props),
      ExternalFacilityIdentifier: props.GlobalId.value,
      Description: getAttributeOf(props, "Description", null) || getAttributeOf(props, "LongName"),
      ProjectDescription: getAttributeOf(project, "Description", null) || getAttributeOf(project, "LongName"),
      SiteDescription: getAttributeOf(site, "Description", null) || getAttributeOf(site, "LongName"),
      Phase: getAttributeOf(project, "Phase")
    });
  };
  
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Facility", sheet: sheet};
}

async function getFloors(ifcApi, modelID, context) {
  const floors = await ifcApi.getAllItemsOfType(modelID, IFCBUILDINGSTOREY, true);
  const rows = [];
  for(const floor of floors) {
    const props = await fetchProperties(ifcApi, modelID, floor.expressID, true, true);
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn: props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(floor, context, "FloorType"),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      Description: getAttributeOf(props, "Description", null) || getAttributeOf(props, "LongName"),
      Elevation: getAttributeOf(props, "Elevation"),
      Height: getAttributeOf(props, "Height"),
    });
  };
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Floor", sheet: sheet};
}

function getPSetValueFromObject(objectProps, pSetName, propName, context, pickList, defaultValue = "n/a") {
  const psets = objectProps.pset || [];
  const pset = psets.find( (pset) => pset.Name?.value == pSetName);
  if ( pset ) {
    const res = pset[propName]?.value || defaultValue;
    if ( pickList ) {
      context.pickLists[pickList].push(res);
    }
    return res;
  } else {
    if ( pickList ) {
      context.pickLists[pickList].push(defaultValue);
    }
    return defaultValue;
  }

}

function getUsableHeightFromSpace() {
  return "TODO";
}

function getGrossAreaFromSpace() {
  return "TODO";
}

function getNetAreaFromSpace() {
  return "TODO";
}

async function getSpaces(ifcApi, modelID, context) {
  const spaces = await ifcApi.getAllItemsOfType(modelID, IFCSPACE, true);
  const rows = [];
  for(const space of spaces) {
    const props = await fetchProperties(ifcApi, modelID, space.expressID, true, true);
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn: props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(space, context, "Category-Space"),
      FloorName: getAttributeOf(await getAncestorPropertiesOfType(ifcApi, modelID, space.expressID, context, "IFCBUILDINGSTOREY"), "Name"),
      Description: getAttributeOf(props, "Description", null) || getAttributeOf(props, "LongName"),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      RoomTag: getPSetValueFromObject(props, "COBie_Space", "RoomTag"),
      UsableHeight: getUsableHeightFromSpace(props),
      GrossArea: getGrossAreaFromSpace(props),
      NetArea: getNetAreaFromSpace(props),
    });
  };


  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Space", sheet: sheet};
}

function isKindOf(obj, type) {
  let inheritance = [];
  let tmp = obj;
  while ( tmp.constructor.name !== 'Object') {
    inheritance.push(tmp.constructor.name);
    tmp = Object.getPrototypeOf(tmp);
  }
  return inheritance.indexOf(type) >= 0;
}

function  getGroupedProductNamesFromObject(props, type) {
  return (props.IsGroupedBy || [])
        .filter( (obj) =>  isKindOf(obj, type))
        .map((obj) => obj.Name.value)
        .join(",");
  /*
  .map( 
    (relObject) => (relObject.RelatedObjects || [])
      .filter((obj) =>)
      .map ((obj) => obj.Name.value))
    .join(",");
    */

}

async function getZones(ifcApi, modelID, context) {
  const zones = await ifcApi.getAllItemsOfType(modelID, IFCZONE, true);
  const rows = [];
  for(const zone of zones) {
    const props = await fetchProperties(ifcApi, modelID, zone.expressID, true, true, {relation: "IsGroupedBy", type: IFCRELASSIGNSTOGROUP});
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn: props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(zone, context, "ZoneType"),
      SpaceNames: getGroupedProductNamesFromObject(zone, "IfcSpace"),
      Description: getAttributeOf(props, "Description", null) || getAttributeOf(props, "LongName"),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
    });
  };

  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Zone", sheet: sheet};
}

async function getTypes(ifcApi, modelID, context) {
  const types = await ifcApi.getAllItemsOfType(modelID, IFCTYPEOBJECT, true);
  const rows = [];
  for(const type of types) {
    const props = await fetchProperties(ifcApi, modelID, type.expressID, true, true);
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn: props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(type, context, "Category-Product"),
      Description: getAttributeOf(props, "Description", null) || getAttributeOf(props, "LongName"),
      AssetType: getPSetValueFromObject(type, "COBie_Asset", "AssetType", context, "AssetType"),
      Manufacturer: getPSetValueFromObject(type, "Pset_ManufacturerTypeInformation", "Manufacturer", context),
      ModelNumber: getPSetValueFromObject(type, "Pset_ManufacturerTypeInformation", "ModelNumber", context),
      WarrantyGuarantorParts: getPSetValueFromObject(type, "COBie_Warranty", "WarrantyGuarantorParts", context),
      WarrantyDurationParts: getPSetValueFromObject(type, "COBie_Warranty", "WarrantyDurationParts", context),
      WarrantyGuarantorLabor: getPSetValueFromObject(type, "COBie_Warranty", "WarrantyGuarantorLabor", context),
      WarrantyDurationLabor: getPSetValueFromObject(type, "COBie_Warranty", "WarrantyDurationLabor", context, null, 0),
      WarrantyDurationUnit: "day",
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      ReplacementCost: getPSetValueFromObject(type, "COBie_EconomicImpactValues", "ReplacementCost", context),
      ExpectedLife: getPSetValueFromObject(type, "Pset_ServiceLife", "ServiceLifeDuration", context, null, null ) ||
                    getPSetValueFromObject(type, "COBie_ServiceLife", "ServiceLifeDuration", context, null ),
      DurationUnit: "day",
      NominalLength: getPSetValueFromObject(type, "COBie_Specification", "NominalLength", context),
      NominalWidth: getPSetValueFromObject(type, "COBie_Specification", "NominalWidth", context),
      NominalHeight: getPSetValueFromObject(type, "COBie_Specification", "NominalHeight", context),
      ModelReference: getPSetValueFromObject(type, "COBie_Specification", "ModelReference", context),
      Shape: getPSetValueFromObject(type, "COBie_Specification", "Shape", context),
      Size: getPSetValueFromObject(type, "COBie_Specification", "Size", context),
      Color: getPSetValueFromObject(type, "COBie_Specification", "Color", context),
      Finish: getPSetValueFromObject(type, "COBie_Specification", "Finish", context),
      Grade: getPSetValueFromObject(type, "COBie_Specification", "Grade", context),
      Material: getPSetValueFromObject(type, "COBie_Specification", "Material", context),
      Constituents: getPSetValueFromObject(type, "COBie_Specification", "Constituents", context),
      Features: getPSetValueFromObject(type, "COBie_Specification", "Features", context),
      AccessibilityPerformance: getPSetValueFromObject(type, "COBie_Specification", "AccessibilityPerformance", context),
      CodePerformance: getPSetValueFromObject(type, "COBie_Specification", "CodePerformance", context),
      SustainabilityPerformance: getPSetValueFromObject(type, "COBie_Specification", "SustainabilityPerformance", context)
  
    });
  } 
  
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Type", sheet: sheet};
}

async function getComponents(ifcApi, modelID, context) {
  const components = await ifcApi.getAllItemsOfType(modelID, IFCELEMENT, true);
  const rows = [];
  for( const component of components) {
    const props = await fetchProperties(ifcApi, modelID, component.expressID, true, true);
    rows.push({
      Name: props.Name?.value,
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn:  props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      TypeName: (props.IsTypedBy || []).filter( (rel) => rel.RelatingType.Name )[0]?.RelatingType.Name ||
                (props.IsDefinedBy || []).filter( (rel ) => rel.constructor.name === "IfcRelDefinesByType" && rel.RelatingType.Name)[0]?.RelatingType.Name || "n/a",
      Space: (props.ContainedInStructure || []).filter( (rel) => rel.constructor.name === "IfcSpace" && rel.RelatingStructure.Name)[0]?.RelatingStructure.Name || "n/a",
      Description: getAttributeOf(props, "Description"),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      SerialNumber: getPSetValueFromObject(props, "Pset_ManufacturerOccurence", "SerialNumber", context),
      InstallationDate: getPSetValueFromObject(props, "COBie_Component", "InstallationDate",context, null, new Date().toISOString()),
      WarrantyStartDate: getPSetValueFromObject(props, "COBie_Component", "WarrantyStartDate", context, null, new Date().toISOString()),
      TagNumber: getPSetValueFromObject(props, "COBie_Component", "TagNumber", context),
      BarCode: getPSetValueFromObject(props, "Pset_ManufacturerOccurence", "BarCode", context),
      AssetIdentifier: getPSetValueFromObject(props, "COBie_Component", "AssetIdentifier", context)
    });
  }
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Component", sheet: sheet};
}

async function getSystems(ifcApi, modelID, context) {
  const systems = await ifcApi.getAllItemsOfType(modelID, IFCSYSTEM, true);
  const rows = [];
  for(const system of systems) {
    const props = await fetchProperties(ifcApi, modelID, system.expressID, true, true, { relation: "IsGroupedBy", type: IFCRELASSIGNSTOGROUP, checkFor: "RelatingGroup" });
    rows.push({
      Name: props.Name?.value, 
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn:  props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(system, context, "Category-Element"),
      ComponentNames: getGroupedProductNamesFromObject(props, "IfcProduct"),
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      Description: getAttributeOf(props, "Description", null ) || getAttributeOf(props, "LongName")
    });
  }

  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "System", sheet: sheet};
}

async function getAssemblies(ifcApi, modelID, context) {
  const assemblies = await ifcApi.getAllItemsOfType(modelID, IFCRELAGGREGATES, true);
  const rows = [];
  for(const assembly of assemblies) {
    const props = await fetchProperties(ifcApi, modelID, assembly.expressID, true, true);
    rows.push({
      Name: props.Name?.value, 
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn:  props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      SheetName: "Assembly",
      ParentName: props.RelatingObject?.Name?.value || "n/a",
      ChildNames: (assembly.RelatedObjects || []).map( (rel) => rel.Name?.value).join(","),
      AssemblyType: "n/a",
      ExternalSystem: getExtSystemFromHistory(props.OwnerHistory),
      ExtObject: getExtObjectFrom(props),
      ExtIdentifier: props.GlobalId.value,
      Description: getAttributeOf(props, "Description")
    });
  }
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Assembly", sheet: sheet};
}

async function getSpares(ifcApi, modelID, context) {
  const spares = await ifcApi.getAllItemsOfType(modelID, IFCCONSTRUCTIONPRODUCTRESOURCE, true);
  const rows = [];
  for(const spare of spares) {
    const props = await fetchProperties(ifcApi, modelID, spare.expressID, true, true, true);
    rows.push({
      Name: props.Name?.value, 
      CreatedBy: getEmailFromHistory(props.OwnerHistory),
      CreatedOn:  props.OwnerHistory.CreationDate ? new Date(1000 * props.OwnerHistory.CreationDate.value).toISOString() : context.defaultDate.toISOString(),
      Category: getCategoryFromObject(spare, context, "SpareType"),
      TypeName: (props.IsTypedBy || []).filter( (rel) => rel.RelatingType.Name )[0]?.RelatingType.Name ||
                (props.IsDefinedBy || []).filter( (rel ) => rel.constructor.name === "IfcRelDefinesByType" && rel.RelatingType.Name)[0]?.RelatingType.Name || "n/a",
      Suppliers: getPSetValueFromObject(spare, "COBie_Spare", "Suppliers"),
      ExtSystem: getExtSystemFromHistory(spare.OwnerHistory),
      ExtObject: getExtObjectFrom(spare),
      ExtIdentifier: spare.GlobalId.value,
      Description: getAttributeOf(spare, "Description"),
      SetNumber: getPSetValueFromObject(spare, "COBie_Spare", "SetNumber"),
      PartNumber: getPSetValueFromObject(spare, "COBie_Spare", "PartNumber")
    });
  }
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Spare", sheet: sheet};
}

async function getResources(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Resource", sheet: sheet};
}

async function getJobs(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Job", sheet: sheet};
}

async function getImpacts(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Impact", sheet: sheet};
}

async function getDocuments(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Document", sheet: sheet};
}

async function getAttributes(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Attribute", sheet: sheet};
}

async function getCoordinates(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Coordinate", sheet: sheet};
}

async function getConnections(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Connection", sheet: sheet};
}

async function getIssues(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "Issue", sheet: sheet};
}

async function getPickLists(ifcApi, modelID, context) {
  const rows = [];
  const sheet = XLSX.utils.json_to_sheet(rows);
  return {sheetName: "PickLists", sheet: sheet};
}

export async function modelToCobie(ifcApi, modelID, spatialStructure, creatorInformation = {}) {

  const tabs = [
    getContacts,
    getFacilities,
    getFloors,
    getSpaces,
    getZones,
    getTypes,
    getComponents,
    getSystems,
    getAssemblies,
    getSpares,
    getResources,
    getJobs,
    getImpacts,
    getDocuments,
    getAttributes,
    getCoordinates,
    getConnections,
    getIssues,
    getPickLists
  ];

  
  const context = {
      spatialStructure: {
        hierarchy: spatialStructure,
        flatten: {}
      },
      pickLists: {
        "Category-Role": [],
        "Category-Facility": [],
        "FloorType": [],
        "Category-Space": [],
        "ZoneType": [],
        "Category-Product": [],
        "AssetType": [],
        "DurationUnit": ["day"],
        "Category-Element": [],
        "SpareType": [],
        "ApprovalBy": [],
        "StageType": [],
        "objType": []
      },
    defaultDate: new Date()
  };

  var workbook = XLSX.utils.book_new();
  for(const tab of tabs) {
    const {sheetName, sheet } = await tab(ifcApi, modelID, context);
    XLSX.utils.book_append_sheet(workbook, sheet, sheetName);
  }; 
  
  var xlsbin = XLSX.write(workbook, {
    bookType: "xlsx",
    type: "binary"
  });
   
  // (C4) TO BLOB OBJECT
  var buffer = new ArrayBuffer(xlsbin.length),
      array = new Uint8Array(buffer);
  for (var i=0; i<xlsbin.length; i++) {
    array[i] = xlsbin.charCodeAt(i) & 0XFF;
  }
  var xlsblob = new Blob([buffer], {type:"application/octet-stream"});
  return xlsblob;


    const project = (await ifcApi.getAllItemsOfType(modelID, IFCPROJECT, true))[0];
    const projectProperties = await ifcApi.getProperties(modelID, project.expressID, false, true);
    
    /*
    person = history.OwningUser.ThePerson
        organisation = history.OwningUser.TheOrganization
        email = self.get_email_from_person_or_organisation(person)
        if email:
            return email

        email = self.get_email_from_person_or_organisation(organisation)
        if email:
            return email

        given_name = person.GivenName if person.GivenName else "unknown"
        family_name = person.FamilyName if person.FamilyName else "unknown"
        organisation_name = organisation.Name if organisation.Name else "unknown"

        if given_name == "unknown" and family_name == "unknown" and organisation_name == "unknown":
            self.logger.error("No primary key could be determined from %s", history)

        return "{}{}@{}".format(given_name, family_name, organisation_name)

     */
    const createdBy = creatorInformation.createdBy || "IfcJS-Maximo-Bim-Viewer User";
    const createdOn = projectProperties["OwnerHistory"]["CreationDate"]?.value || (new Date().getTime() / 1000);
    const externalSystem = projectProperties["OwnerHistory"]

    var workbook = XLSX.utils.book_new();
      //var worksheet = XLSX.utils.aoa_to_sheet(data);
      // Generate instruction tab
      workbook.SheetNames.push("Instruction");
      // Generate contact tab
      var rows = [
        {
          Email: null, CreatedBy: createdBy, CreatedOn: createdOn, Category: null, Company: null,
          Phone: null, ExtSystem: null, ExtObject: null, ExtIdentifier: null, Department: null,
          OrganizationCode: null, GivenName: null, FamilyName: null, Street: null, PostalBox: null,
          Town: null, StateRegion: null, PostalCode: null, Country: null
        }
      ];

      var {sheetName, sheet } = getContacts(ifcApi, modelID);
      XLSX.utils.book_append_sheet(workbook, sheet, sheetName);
        

      // Generate facility tab
      //Name	CreatedBy	CreatedOn	Category	ProjectName	SiteName	LinearUnits	AreaUnits	VolumeUnits	CurrencyUnit	AreaMeasurement	ExternalSystem	ExternalProjectObject	ExternalProjectIdentifier	ExternalSiteObject	ExternalSiteIdentifier	ExternalFacilityObject	ExternalFacilityIdentifier	Description	ProjectDescription	SiteDescription	Phase
      
      const site = (await ifcApi.getAllItemsOfType(modelID, IFCSITE, true))[0];
      const buildings = await ifcApi.getAllItemsOfType(modelID, IFCBUILDING, true);
      rows = buildings.map( async function(building) {
        const props = await ifcApi.getProperties(modelID, building.expressID, false, true);
        return {
          name : building["Name"]?.value,
          createdby: building["CreatedBy"]?.value,
          ProjectName: project["Name"]?.value?.length > 0 ? project["Name"]?.value : project["LongName"]?.value,
          SiteName: site["Name"]?.value?.length > 0 ? site["Name"]?.value : site["LongName"]?.value,
          LinearUnits: linearUnits,
          AreaUnits: areaUnits,
          VolumeUnits: volumeUnits,
          CurrencyUnit: currencyUnit,
          AreaMeasurement: areaMeasurement,
          ExternalSystem: externalSystem,
          ExternalProjectObject: "IfcProject",
          ExternalProjectIdentifier: project["GlobalId"]?.value,
          ExternalSiteObject: "IfcSite",
          ExternalSiteIdentifier: site["GlobalId"]?.value,
          ExternalFacilityObject: "IfcBuilding",
          ExternalFacilityIdentifier: building["GlobalId"]?.value,
          Description:	building["Description"]?.value,
          ProjectDescription: project["Description"]?.value,
          SiteDescription: site["Description"]?.value,
          Phase: project["Phase"].value
        
        };
      });

      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Facility");

      // Generate Floor tab
      const floors = await ifcApi.getAllItemsOfType(modelID, IFCBUILDINGSTOREY, true);
      rows = floors.map( function(floor) {
        return {
          Name: floor["Name"].value,
          CreatedBy: createdBy,
          CreatedOn: createdOn,
          Category: floor["ObjectType"]?.value || "n/a",
          ExtSystem: externalSystem,
          ExtObject: "IfcBuildingStorey",
          ExtIdentifier: floor["GlobalId"].value,
          Description: floor["Description"]?.value || "n/a",
          Elevation: floor["Elevation"]?.value || "n/a",
          Height: floor["Height"]?.value || "n/a"
        };
      });
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Floor");

      // Generate Space tab
      const spaces = await ifcApi.getAllItemsOfType(modelID, IFCSPACE, true);
      rows = spaces.map( function(space) {
        return {
          //Name	CreatedBy	CreatedOn	Category	FloorName	Description	ExtSystem	ExtObject	ExtIdentifier	RoomTag	UsableHeight	GrossArea	NetArea
          Name: space["Name"]?.value,
          CreatedBy: createdBy,
          CreatedOn: createdOn,
          Category: space["ObjectType"]?.value,
          FloorName: null,
          Description: space["Description"]?.value || space["LongName"]?.value,
          ExtSystem: externalSystem,
          ExtObject: "IfcSpace",
          ExtIdentifier: space["GlobalId"].value,
          RoomTag: space["RoomTag"]?.value,
          UsableHeight: space["UsableHeight"]?.value,
          GrossArea: space["GrossArea"]?.value,
          NetArea: space["NetArea"]?.value
        };
      });
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Space");

      // Zone
      //Name	CreatedBy	CreatedOn	Category	SpaceNames	ExtSystem	ExtObject	ExtIdentifier	Description
      const zones = await ifcApi.getAllItemsOfType(modelID, IFCZONE, true);
      rows = zones.map( function(zone) {
        return {
          Name: zone["Name"]?.value,
          CreatedBy: createdBy,
          CreatedOn: createdOn,
          Category: zone["ObjectType"]?.value || "n/a",
          SpaceNames: "n/a",
          ExtSystem: externalSystem,
          ExtObject: "IfcZone",
          ExtIdentifier: zone["GlobalId"].value,
          Description: zone["Description"]?.value || "n/a"
        };
      });
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Zone");

      // Type
      /* 
        Name: material["Name"]?.value,
        CreatedBy: createdBy,
        CreatedOn: createdOn,
        Category: material["ObjectType"]?.value || "n/a",
        Description: material["Description"]?.value || "n/a",
        AssetType: 
        Manufacturer	
        ModelNumber	
        WarrantyGuarantorParts	
        WarrantyDurationParts	
        WarrantyGuarantorLabor	
        WarrantyDurationLabor	
        WarrantyDurationUnit	
        ExtSystem	
        ExtObject	
        ExtIdentifier	
        ReplacementCost	
        ExpectedLife	
        DurationUnit	
        WarrantyDescription	
        NominalLength	
        NominalWidth	
        NominalHeight	
        ModelReference	
        Shape	
        Size	
        Color	
        Finish	
        Grade	
        Material	
        Constituents	
        Features	
        AccessibilityPerformance	
        CodePerformance	
        SustainabilityPerformance
        */
      const types = await ifcApi.getAllItemsOfType(modelID, IFCMATERIAL, true);
      const products = await ifcApi.getAllItemsOfType(modelID, IFCPRODUCT, true);
      const equipments = await ifcApi.getAllItemsOfType(modelID, IFCUNITARYEQUIPMENT, true);

      rows = [
        types.map( function(material) {
          return {
            Name: material["Name"]?.value,
            CreatedBy: createdBy,
            CreatedOn: createdOn,
            Category: material["ObjectType"]?.value || "n/a",
            Description: material["Description"]?.value || "n/a",
            
          };
        }), 
        ...types.map( function(material) {
          return {
            
          };
        }),
        ...equipments.map( function(equipment) {
          return {
            
          };
        })
      ];
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Type");

      // Component
      workbook.SheetNames.push("Component");
      
      // System
      const systems = await ifcApi.getAllItemsOfType(modelID, IFCSYSTEM, true);
      rows = systems.map( function(system) {
        return {
          Name: system["Name"]?.value,
          CreatedBy: "",
          CreatedOn: "",
          Category: system["ObjectType"]?.value || "n/a",
          ComponentNames: "n/a",
          ExtSystem: externalSystem,
          ExtObject: "IfcSystem",
          ExtIdentifier: system["GlobalId"].value,
          Description: system["Description"]?.value || "n/a",
        };
      });
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "System");
      //Name	CreatedBy	CreatedOn	Category	ComponentNames	ExtSystem	ExtObject	ExtIdentifier	Description

      // Assembly
      const assemblies = await ifcApi.getAllItemsOfType(modelID, IFCELEMENTASSEMBLY, true);
      rows = systems.map( function(assembly) {
        return {
          Name: assembly["Name"]?.value,
          CreatedBy: "",
          CreatedOn: "",
          Category: assembly["ObjectType"]?.value || "n/a",
          ComponentNames: null,
          ExtSystem: externalSystem,
          ExtObject: "IfcElementAssembly",
          ExtIdentifier: assembly["GlobalId"].value || "n/a",
          Description: assembly["Description"]?.value || "n/a"
        };
      });
      XLSX.utils.book_append_sheet(workbook, XLSX.utils.json_to_sheet(rows), "Assembly");

      // Connection
      workbook.SheetNames.push("Connection");
      // Spare
      workbook.SheetNames.push("Spare");
      // Resource
      workbook.SheetNames.push("Resource");
      // Job
      workbook.SheetNames.push("Job");
      // Impact
      workbook.SheetNames.push("Impact");
      // Document
      workbook.SheetNames.push("Document");
      // Attribute
      workbook.SheetNames.push("Attribute");
      // Coordinate
      workbook.SheetNames.push("Coordinate");
      // Issue
      workbook.SheetNames.push("Issue");
      // PickLists
      workbook.SheetNames.push("PickLists");

      var xlsbin = XLSX.write(workbook, {
        bookType: "xlsx",
        type: "binary"
      });
       
      // (C4) TO BLOB OBJECT
      var buffer = new ArrayBuffer(xlsbin.length),
          array = new Uint8Array(buffer);
      for (var i=0; i<xlsbin.length; i++) {
        array[i] = xlsbin.charCodeAt(i) & 0XFF;
      }
      var xlsblob = new Blob([buffer], {type:"application/octet-stream"});
      return xlsblob;
      /*
      var url = window.URL.createObjectURL(xlsblob),
      anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "cobie.xlsx";
      anchor.click();
      window.URL.revokeObjectURL(url);
      */
}