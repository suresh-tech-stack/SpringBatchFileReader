package com.conduent.iral.batch.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * This Class is for Mapping the T_MOMS_MESSAGE_PRIORITY Table data into
 * MomsData Object
 * 
 * @author 52058018
 *
 */
public class MomsDataMapper implements RowMapper<MomsData> {
	@Override
	public MomsData mapRow(ResultSet rs, int rowNum) throws SQLException {
		MomsData momsData = new MomsData();
		momsData.setEquipmentId(rs.getLong("EQUIPMENT_ID"));
		momsData.setRepairNumber(rs.getLong("REPAIR_NUMBER"));
		momsData.setDescription(rs.getString("DESCRIPTION"));
		return momsData;
	}
}
