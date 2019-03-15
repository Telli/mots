import React, { Component } from 'react';
import {
  View,
  Text,
  ScrollView,
  PixelRatio,
} from 'react-native';
import { Select, Option } from 'react-native-chooser';
import { TagSelect } from 'react-native-tag-select';
import PropTypes from 'prop-types';
import { Actions } from 'react-native-router-flux';
import { connect } from 'react-redux';
import DatePicker from 'react-native-datepicker';

import apiClient from '../utils/api-client';
import formsStyles from '../styles/formsStyles';
import modulesStyles from '../styles/modulesStyles';
import inputsStyles from '../styles/inputsStyles';
import Button from './Button';
import getContainerStyle from '../utils/styleUtils';
import commonStyles from '../styles/commonStyles';

const { formHeader, buttonContainer } = formsStyles;
const { labelStyle, labelStyleSmall } = inputsStyles;
const {
  modulesContainer, itemSelected,
  fieldRow, selectField, datePickerStyle, dateInput,
} = modulesStyles;
const { lightThemeText } = commonStyles;

class AssignModulesToGroup extends Component {
  constructor(props) {
    super(props);
    this.state = {
      availableModulesList: [],
      groups: [],
      selectedGroup: {},
      startDate: '',
      endDate: '',
    };

    this.onSelect = this.onSelect.bind(this);
    this.sendAssignedModules = this.sendAssignedModules.bind(this);
  }

  componentWillMount() {
    this.fetchGroups();
    this.fetchAvailableModules();
  }

  onSelect(value, label) {
    this.setState({
      selectedGroup: {
        value, label,
      },
    });
  }

  fetchGroups() {
    apiClient.get('/api/group')
      .then((response) => {
        if (response) {
          const groups = response.map(group => ({ value: group.id, label: group.name }));
          this.setState({ groups });
        }
      });
  }

  fetchAvailableModules() {
    const url = '/api/modules/simple';

    apiClient.get(url)
      .then((response) => {
        const availableModulesList = response.map(module => (
          { id: module.id, label: module.name }
        ));
        if (response) {
          this.setState({ availableModulesList });
        }
      });
  }

  sendAssignedModules(selectedModules) {
    if (this.state.selectedGroup.value && selectedModules &&
        this.state.startDate && this.state.endDate) {
      const url = '/api/module/group/assign';

      const payload = {
        modules: selectedModules.map(module => module.id),
        groupId: this.state.selectedGroup.value,
        startDate: this.state.startDate,
        endDate: this.state.endDate,
      };

      const callback = () => {
        if (!this.props.fetchError) {
          Actions.modalSuccess({
            message: 'Modules have been assigned!',
            onClose: () => { Actions.chws(); },
          });
        }
      };

      apiClient.post(url, payload)
        .then(() => callback());
    } else {
      Actions.modalInfo({
        message: 'You need to select group, start date, end date ' +
        'and module to finish assignment.',
      });
    }
  }

  render() {
    return (
      <View style={getContainerStyle()}>
        <Text style={[formHeader, lightThemeText]}>Assign Modules to a Group</Text>
        <ScrollView style={modulesContainer} alwaysBounceVertical={false}>
          <View style={fieldRow}>
            <Text style={[labelStyle, lightThemeText, PixelRatio.get() < 2 && labelStyleSmall]}>
              Group:
            </Text>
            <View style={selectField}>
              <Select
                onSelect={this.onSelect}
                defaultText={this.state.selectedGroup.label || 'Click to Select'}
                textStyle={lightThemeText}
                style={{ borderWidth: 0 }}
                transparent
                optionListStyle={{ backgroundColor: '#FFF' }}
              >
                {this.state.groups.map(group => (
                  <Option key={group.value} value={group.value} styleText={lightThemeText}>
                    {group.label}
                  </Option>
                ))}
              </Select>
            </View>
          </View>
          <View style={fieldRow}>
            <Text style={[
              labelStyle,
              lightThemeText,
              PixelRatio.get() < 2 && labelStyleSmall]}
            >
              Start Date:
            </Text>
            <DatePicker
              style={datePickerStyle}
              format="YYYY-MM-DD"
              timeFormat={false}
              closeOnSelect
              placeholder="Select a date"
              confirmBtnText="Confirm"
              cancelBtnText="Cancel"
              customStyles={{
                placeholderText: lightThemeText,
                dateInput,
              }}
              date={this.state.startDate}
              onDateChange={(date) => { this.setState({ startDate: date }); }}
            />
          </View>
          <View style={fieldRow}>
            <Text style={[
              labelStyle,
              lightThemeText,
              PixelRatio.get() < 2 && labelStyleSmall]}
            >
              End Date:
            </Text>
            <DatePicker
              style={datePickerStyle}
              format="YYYY-MM-DD"
              timeFormat={false}
              closeOnSelect
              placeholder="Select a date"
              confirmBtnText="Confirm"
              cancelBtnText="Cancel"
              customStyles={{
                placeholderText: lightThemeText,
                dateInput,
              }}
              date={this.state.endDate}
              onDateChange={(date) => { this.setState({ endDate: date }); }}
            />
          </View>
          <View>
            <Text style={[
              labelStyle,
              lightThemeText,
              PixelRatio.get() < 2 && labelStyleSmall,
              { marginBottom: 15 },
            ]}
            >
              Select modules to assign:
            </Text>
            <TagSelect
              data={this.state.availableModulesList}
              ref={(module) => { this.module = module; }}
              itemStyleSelected={itemSelected}
            />
            <View style={buttonContainer}>
              <Button
                iconName="check"
                iconColor="#FFF"
                buttonColor="#337ab7"
                onPress={() => {
                  this.sendAssignedModules(this.module.itemsSelected);
                }}
              >
                Assign
              </Button>
              <Button
                iconName="ban"
                iconColor="#FFF"
                buttonColor="grey"
                style={{ marginLeft: 10 }}
                onPress={() => {
                  Actions.chws();
                }}
              >
                Cancel
              </Button>
            </View>
          </View>
        </ScrollView>
      </View>

    );
  }
}

function mapStateToProps(state) {
  return {
    fetchError: state.tablesReducer.fetchError,
  };
}

export default connect(mapStateToProps)(AssignModulesToGroup);

AssignModulesToGroup.propTypes = {
  fetchError: PropTypes.bool.isRequired,
};